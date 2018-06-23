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

import org.apache.geronimo.config.converters.*;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import javax.annotation.Priority;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Typed
@Vetoed
public class ConfigImpl implements Config, AutoCloseable {

    protected Logger logger = Logger.getLogger(ConfigImpl.class.getName());

    protected List<ConfigSource> configSources = new ArrayList<>();
    protected Map<Type, Converter> converters = new HashMap<>();
    protected Map<Type, Converter> implicitConverters = new ConcurrentHashMap<>();

    // volatile to a.) make the read/write behave atomic and b.) guarantee multi-thread safety
    private volatile long lastChanged = 0;

    public ConfigImpl() {
        registerDefaultConverter();
    }

    private void registerDefaultConverter() {
        converters.put(String.class, StringConverter.INSTANCE);
        converters.put(Boolean.class, BooleanConverter.INSTANCE);
        converters.put(boolean.class, BooleanConverter.INSTANCE);
        converters.put(Double.class, DoubleConverter.INSTANCE);
        converters.put(double.class, DoubleConverter.INSTANCE);
        converters.put(Float.class, FloatConverter.INSTANCE);
        converters.put(float.class, FloatConverter.INSTANCE);
        converters.put(Integer.class, IntegerConverter.INSTANCE);
        converters.put(int.class, IntegerConverter.INSTANCE);
        converters.put(Long.class, LongConverter.INSTANCE);
        converters.put(long.class, LongConverter.INSTANCE);

        converters.put(Class.class, ClassConverter.INSTANCE);
        converters.put(URL.class, URLConverter.INSTANCE);
    }

    @Override
    public <T> T getOptionalValue(String propertyName, Class<T> asType) {
        String value = getValue(propertyName);
        if (value != null && value.length() == 0) {
            // treat an empty string as not existing
            value = null;
        }
        return convert(value, asType);
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        String value = getValue(propertyName);
        if (value == null || value.isEmpty()) {
            throw new NoSuchElementException("No configured value found for config key " + propertyName);
        }

        return convert(value, propertyType);
    }

    public String getValue(String key) {
        for (ConfigSource configSource : configSources) {
            String value = configSource.getValue(key);

            if (value != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "found value {0} for key {1} in ConfigSource {2}.",
                            new Object[]{value, key, configSource.getName()});
                }

                return value;
            }
        }

        return null;
    }

    public <T> T convert(String value, Class<T> asType) {
        if (value != null) {
            Converter<T> converter = getConverter(asType);
            return converter.convert(value);
        }

        return null;
    }

    private <T> Converter getConverter(Class<T> asType) {
        Converter converter = converters.get(asType);
        if (converter == null) {
            converter = getImplicitConverter(asType);
        }
        if (converter == null) {
            throw new IllegalArgumentException("No Converter registered for class " + asType);
        }
        return converter;
    }

    private <T> Converter getImplicitConverter(Class<T> asType) {
        Converter converter = implicitConverters.get(asType);
        if (converter == null) {
            synchronized (implicitConverters) {
                converter = implicitConverters.get(asType);
                if (converter == null) {
                    if (asType.isArray()) {
                        Converter singleItemConverter = getConverter(asType.getComponentType());
                        if (singleItemConverter == null) {
                            return null;
                        } else {
                            converter = new ImplicitConverter.ImplicitArrayConverter(singleItemConverter, asType.getComponentType());
                            keepImplicitConverer(asType, converter);
                        }
                    } else {
                        // try to check whether the class is an 'implicit converter'
                        converter = ImplicitConverter.getImplicitConverter(asType);
                        if (converter != null) {
                            keepImplicitConverer(asType, converter);
                        }
                    }
                }
            }
        }
        return converter;
    }

    private <T> void keepImplicitConverer(Class<T> asType, Converter converter) {
        // putIfAbsent from Java 8
        if (implicitConverters.get(asType) == null) {
            implicitConverters.put(asType, converter);

        }
    }

    public ConfigValueImpl<String> access(String key) {
        return new ConfigValueImpl<>(this, key);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> result = new HashSet<>();

        for (ConfigSource configSource : configSources) {
            result.addAll(configSource.getProperties().keySet());

        }
        return result;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return Collections.unmodifiableList(configSources);
    }

    public synchronized void addConfigSources(List<ConfigSource> configSourcesToAdd) {
        List<ConfigSource> allConfigSources = new ArrayList<>(configSources);
        allConfigSources.addAll(configSourcesToAdd);

        // finally put all the configSources back into the map
        configSources = sortDescending(allConfigSources);
    }

    public synchronized void addConverter(Converter<?> converter) {
        if (converter == null) {
            return;
        }

        Type targetType = getTypeOfConverter(converter.getClass());
        if (targetType == null) {
            throw new IllegalStateException("Converter " + converter.getClass() + " must be a ParameterisedType");
        }

        Converter oldConverter = converters.get(targetType);
        if (oldConverter == null || getPriority(converter) > getPriority(oldConverter)) {
            converters.put(targetType, converter);
        }
    }

    public void addPrioritisedConverter(DefaultConfigBuilder.PrioritisedConverter prioritisedConverter) {
        Converter oldConverter = converters.get(prioritisedConverter.getType());
        if (oldConverter == null || prioritisedConverter.getPriority() >= getPriority(oldConverter)) {
            converters.put(prioritisedConverter.getType(), prioritisedConverter.getConverter());
        }
    }

    private int getPriority(Converter<?> converter) {
        int priority = 100;
        Priority priorityAnnotation = converter.getClass().getAnnotation(Priority.class);
        if (priorityAnnotation != null) {
            priority = priorityAnnotation.value();
        }
        return priority;
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

    public Map<Type, Converter> getConverters() {
        return converters;
    }

    @Override
    public void close() throws Exception {
        List<Exception> exceptions = new ArrayList<>();

        for (Converter converter : converters.values()) {
            if (converter instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) converter).close();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        }

        for (ConfigSource configSource : configSources) {
            if (configSource instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) configSource).close();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        }

        if (!exceptions.isEmpty()) {
            StringBuilder sb = new StringBuilder(1024);
            sb.append("The following Exceptions got detected while shutting down the Config:\n");
            for (Exception exception : exceptions) {
                sb.append(exception.getClass().getName())
                        .append(" ")
                        .append(exception.getMessage())
                        .append('\n');
            }

            throw new RuntimeException(sb.toString(), exceptions.get(0));
        }
    }

    /**
     * ConfigSources are sorted with descending ordinal.
     * If 2 ConfigSources have the same ordinal, then they get sorted according to their name, alphabetically.
     */
    protected List<ConfigSource> sortDescending(List<ConfigSource> configSources) {
        Collections.sort(configSources, new Comparator<ConfigSource>() {
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2) {
                // ConfigSource.getOrdinal is a default method in original API. When using
                int ordinal1 = getOrdinal(configSource1);
                int ordinal2 = getOrdinal(configSource2);
                if (ordinal1 == ordinal2) {
                    return configSource1.getName().compareTo(configSource2.getName());
                }
                return (ordinal1 > ordinal2) ? -1 : 1;
            }

            private int getOrdinal(ConfigSource configSource) {
                int result = 100;
                try {
                    result = configSource.getOrdinal();
                } catch (AbstractMethodError e) {
                    //
                }
                return result;
            }
        });
        return configSources;

    }

    public void onAttributeChange(Set<String> attributesChanged) {
        // this is to force an incremented lastChanged even on time glitches and fast updates
        long newLastChanged = System.nanoTime();
        lastChanged = lastChanged >= newLastChanged ? lastChanged++ : newLastChanged;
    }

    /**
     * @return the nanoTime when the last change got reported by a ConfigSource
     */
    public long getLastChanged() {
        return lastChanged;
    }

}