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
package org.apache.geronimo.config;

import org.apache.geronimo.config.configsource.PropertyFileConfigSourceProvider;
import org.apache.geronimo.config.configsource.SystemEnvConfigSource;
import org.apache.geronimo.config.configsource.SystemPropertyConfigSource;
import org.apache.geronimo.config.converters.*;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author <a href="mailto:rmannibucau@apache.org">Romain Manni-Bucau</a>
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Typed
@Vetoed
public class DefaultConfigBuilder implements ConfigBuilder {
    private ClassLoader forClassLoader;
    private final List<ConfigSource> sources = new ArrayList<>();
    private boolean ignoreDefaultSources = true;
    private boolean ignoreDiscoveredSources = true;
    private boolean ignoreDiscoveredConverters = true;
    private final Map<Type, MicroProfileTypedConverter<?>> registeredConverters = new HashMap<>();

    public DefaultConfigBuilder() {
        this.registerDefaultConverters();
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        this.ignoreDefaultSources = false;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        this.ignoreDiscoveredSources = false;
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(final ClassLoader loader) {
        this.forClassLoader = loader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(final ConfigSource... sources) {
        this.sources.addAll(asList(sources));
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for (Converter<?> converter : converters) {
            Type typeOfConverter = getTypeOfConverter(converter.getClass());
            registerConverter(typeOfConverter, new MicroProfileTypedConverter<>(converter));
        }
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        MicroProfileTypedConverter<T> microProfileTypedConverter = new MicroProfileTypedConverter<T>(converter, priority);
        return registerConverter(type, microProfileTypedConverter);
    }

    private <T> ConfigBuilder registerConverter(Type type, MicroProfileTypedConverter<T> microProfileTypedConverter) {
        MicroProfileTypedConverter<?> existing = registeredConverters.get(type);
        if (existing == null || microProfileTypedConverter.getPriority() > existing.getPriority()) {
            registeredConverters.put(type, microProfileTypedConverter);
        }
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        ignoreDiscoveredConverters = false;
        return this;
    }

    @Override
    public Config build() {
        List<ConfigSource> configSources = new ArrayList<>();
        if (forClassLoader == null) {
            forClassLoader = Thread.currentThread().getContextClassLoader();
            if (forClassLoader == null) {
                forClassLoader = DefaultConfigProvider.class.getClassLoader();
            }
        }

        if (!ignoreDefaultSources) {
            configSources.addAll(getBuiltInConfigSources(forClassLoader));
        }
        configSources.addAll(sources);

        if (!ignoreDiscoveredSources) {
            // load all ConfigSource services
            ServiceLoader<ConfigSource> configSourceLoader = ServiceLoader.load(ConfigSource.class, forClassLoader);

            for (ConfigSource configSource : configSourceLoader) {
                configSources.add(configSource);
            }

            // load all ConfigSources from ConfigSourceProviders
            ServiceLoader<ConfigSourceProvider> configSourceProviderLoader = ServiceLoader.load(ConfigSourceProvider.class, forClassLoader);

            for (ConfigSourceProvider configSourceProvider : configSourceProviderLoader) {
                for (ConfigSource configSource : configSourceProvider.getConfigSources(forClassLoader)) {
                    configSources.add(configSource);
                }
            }
        }

        if (!ignoreDiscoveredConverters) {

            ServiceLoader<Converter> converterLoader = ServiceLoader.load(Converter.class, forClassLoader);
            for (Converter converter : converterLoader) {
                withConverters(converter);
            }
        }

        ConfigImpl config = new ConfigImpl();
        config.addConfigSources(configSources);

        for (Map.Entry<Type, MicroProfileTypedConverter<?>> entry : registeredConverters.entrySet()) {
            config.addConverter(entry.getKey(), entry.getValue());
        }

        return config;
    }

    protected Collection<? extends ConfigSource> getBuiltInConfigSources(ClassLoader forClassLoader) {
        List<ConfigSource> configSources = new ArrayList<>();

        configSources.add(new SystemEnvConfigSource());
        configSources.add(new SystemPropertyConfigSource());
        // Java EE variant
        configSources.addAll(new PropertyFileConfigSourceProvider("/META-INF/microprofile-config.properties", true, forClassLoader).getConfigSources(forClassLoader));
        // Java SE variant
        configSources.addAll(new PropertyFileConfigSourceProvider("META-INF/microprofile-config.properties", true, forClassLoader).getConfigSources(forClassLoader));

        return configSources;
    }

    private void registerDefaultConverters() {
        registeredConverters.put(String.class, new MicroProfileTypedConverter<>(StringConverter.INSTANCE));
        registeredConverters.put(Boolean.class, new MicroProfileTypedConverter<>(BooleanConverter.INSTANCE));
        registeredConverters.put(boolean.class, new MicroProfileTypedConverter<>(BooleanConverter.INSTANCE));
        registeredConverters.put(Double.class, new MicroProfileTypedConverter<>(DoubleConverter.INSTANCE));
        registeredConverters.put(double.class, new MicroProfileTypedConverter<>(DoubleConverter.INSTANCE));
        registeredConverters.put(Float.class, new MicroProfileTypedConverter<>(FloatConverter.INSTANCE));
        registeredConverters.put(float.class, new MicroProfileTypedConverter<>(FloatConverter.INSTANCE));
        registeredConverters.put(Integer.class, new MicroProfileTypedConverter<>(IntegerConverter.INSTANCE));
        registeredConverters.put(int.class, new MicroProfileTypedConverter<>(IntegerConverter.INSTANCE));
        registeredConverters.put(Long.class, new MicroProfileTypedConverter<>(LongConverter.INSTANCE));
        registeredConverters.put(long.class, new MicroProfileTypedConverter<>(LongConverter.INSTANCE));

        registeredConverters.put(URL.class, new MicroProfileTypedConverter<>(URLConverter.INSTANCE));
        registeredConverters.put(Class.class, new MicroProfileTypedConverter<>(ClassConverter.INSTANCE));
    }

    private Type getTypeOfConverter(Class clazz) {
        if (clazz.equals(Object.class)) {
            return null;
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                if (pt.getRawType().equals(Converter.class)) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length != 1) {
                        throw new IllegalStateException("Converter " + clazz + " must be a ParameterisedType");
                    }
                    return typeArguments[0];
                }
            }
        }

        return getTypeOfConverter(clazz.getSuperclass());
    }
}
