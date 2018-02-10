/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.config.cdi;

import org.apache.geronimo.config.ConfigImpl;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ConfigInjectionBean<T> implements Bean<T>, PassivationCapable {

    private final static Set<Annotation> QUALIFIERS = new HashSet<>();

    static {
        QUALIFIERS.add(new ConfigPropertyLiteral());
    }

    private final BeanManager bm;
    private final Class rawType;
    private final Set<Type> types;
    private final String id;

    /**
     * only access via {@link #getConfig(}
     */
    private Config _config;

    public ConfigInjectionBean(BeanManager bm, Type type) {
        this.bm = bm;
        types = new HashSet<>();
        types.add(type);
        rawType = getRawType(type);
        this.id = "ConfigInjectionBean_" + types;
    }

    private Class getRawType(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;

            return (Class) paramType.getRawType();
        }

        throw new UnsupportedOperationException("No idea how to handle " + type);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Class<?> getBeanClass() {
        return rawType;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> context) {
        InjectionPoint ip = (InjectionPoint) bm.getInjectableReference(new ConfigInjectionPoint(this), context);
        if (ip == null) {
            throw new IllegalStateException("Could not retrieve InjectionPoint");
        }
        Annotated annotated = ip.getAnnotated();
        ConfigProperty configProperty = annotated.getAnnotation(ConfigProperty.class);
        String key = getConfigKey(ip, configProperty);
        String defaultValue = configProperty.defaultValue();

        if (annotated.getBaseType() instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) annotated.getBaseType();
            Type rawType = paramType.getRawType();
            if (rawType instanceof Class && paramType.getActualTypeArguments().length == 1) {
                // handle Provider<T>
                Class<?> rawTypeClass = ((Class<?>) rawType);
                if (rawTypeClass.isAssignableFrom(Provider.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return getConfigValue(key, defaultValue, clazz);
                }

                /*
                // handle Optional<T>
                if (rawTypeClass.isAssignableFrom(Optional.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return (T) getConfig().getOptionalValue(key, clazz);
                }

                if (rawTypeClass.isAssignableFrom(Supplier.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    return (T) new ConfigSupplier(clazz, key, defaultValue, (ConfigImpl)getConfig());
                }
                */

                if (rawTypeClass.isAssignableFrom(Set.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc

                    // read the array type, convert it to a Set
                    ConfigImpl config = (ConfigImpl) getConfig();
                    String value = config.getValue(key);
                    List<Object> elements = config.convertList(value, clazz);
                    return (T) new LinkedHashSet<>(elements);
                }

                if (rawTypeClass.isAssignableFrom(List.class)) {
                    Class clazz = (Class) paramType.getActualTypeArguments()[0]; //X TODO check type again, etc
                    // read the array type, convert it to a List
                    ConfigImpl config = (ConfigImpl) getConfig();
                    String value = config.getValue(key);
                    return (T) config.convertList(value, clazz);
                }
            }

        } else {
            Class clazz = (Class) annotated.getBaseType();
            return getConfigValue(key, defaultValue, clazz);
        }

        throw new IllegalStateException("unhandled ConfigProperty");
    }

    private T getConfigValue(String key, String defaultValue, Class clazz) {
        if (ConfigExtension.isDefaultUnset(defaultValue)) {
            return (T) getConfig().getValue(key, clazz);
        } else {
            Config config = getConfig();
            T optionalValue = (T) config.getOptionalValue(key, clazz);
            return optionalValue == null ? (T) ((ConfigImpl) config).convert(defaultValue, clazz) : optionalValue;

        }
    }

    /**
     * Get the property key to use.
     * In case the {@link ConfigProperty#name()} is empty we will try to determine the key name from the InjectionPoint.
     */
    static String getConfigKey(InjectionPoint ip, ConfigProperty configProperty) {
        String key = configProperty.name();
        if (key.length() > 0) {
            return key;
        }
        if (ip.getAnnotated() instanceof AnnotatedMember) {
            AnnotatedMember member = (AnnotatedMember) ip.getAnnotated();
            AnnotatedType declaringType = member.getDeclaringType();
            if (declaringType != null) {
                return declaringType.getJavaClass().getCanonicalName() + "." + member.getJavaMember().getName();
            }
        }

        throw new IllegalStateException("Could not find default name for @ConfigProperty InjectionPoint " + ip);
    }

    public Config getConfig() {
        if (_config == null) {
            _config = ConfigProvider.getConfig();
        }
        return _config;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context) {

    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return QUALIFIERS;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    private static class ConfigPropertyLiteral extends AnnotationLiteral<ConfigProperty> implements ConfigProperty {
        @Override
        public String name() {
            return "";
        }

        @Override
        public String defaultValue() {
            return "";
        }
    }
}
