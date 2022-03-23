/*
 * Copyright 2017-2022 Rudy De Busscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.config.test;

import be.atbash.config.test.converters.*;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * {@link Config} implementation using a Map for unit test usages. Use {@link TestConfig#addConfigValue(String, String)} to specify some configuration parameters.
 * The default converts are specified with {@link TestConfig#registerDefaultConverters()} and additional ones can be defined using {@link TestConfig#registerConverter(Converter)}.
 * <p>
 * After the unit test has run, one should remove the configuration parameters with the method {@link TestConfig#resetConfig()}.
 */

public class TestConfig implements Config {

    private static final Map<String, String> configValues = new HashMap<>();

    private static final Map<Type, Converter<?>> converters = new HashMap<>();

    /**
     * Add a configuration parameter value, which will be picked up a call to {@link Config#getValue(String, Class)}.
     *
     * @param key   Parameter key value
     * @param value Configuration parameter value.
     */
    public static void addConfigValue(String key, String value) {
        configValues.put(key, value);
    }

    /**
     * Add configuration parameter values, which will be picked up a call to {@link Config#getValue(String, Class)}.
     *
     * @param values Configuration values to add.
     */
    public static void addConfigValues(Map<String, String> values) {
        configValues.putAll(values);
    }

    /**
     * Reset all Configuration parameter values so that the tests keep on being independent.
     */
    public static void resetConfig() {
        configValues.clear();
        converters.clear();
    }

    /**
     * Register a {@link Converter} to convert the Configuration parameter value to a certain type.
     *
     * @param converter The converter to be registered.
     */
    public static void registerConverter(Converter<?> converter) {
        if (converter == null) {
            return;
        }

        Type targetType = getTypeOfConverter(converter.getClass());
        if (targetType == null) {
            throw new IllegalStateException("Converter " + converter.getClass() + " must be a ParameterisedType");
        }

        converters.put(targetType, converter);
    }

    /**
     * Registers default {@link Converter}s for String, Boolean, Double, Float, Integer, Long and Date as defined
     * by the Geronimo Config implementation.
     */
    public static void registerDefaultConverters() {
        converters.put(String.class, StringConverter.INSTANCE);
        converters.put(Boolean.class, BooleanConverter.INSTANCE);
        converters.put(Boolean.TYPE, BooleanConverter.INSTANCE);
        converters.put(Double.class, DoubleConverter.INSTANCE);
        converters.put(Double.TYPE, DoubleConverter.INSTANCE);
        converters.put(Float.class, FloatConverter.INSTANCE);
        converters.put(Float.TYPE, FloatConverter.INSTANCE);
        converters.put(Integer.class, IntegerConverter.INSTANCE);
        converters.put(Integer.TYPE, IntegerConverter.INSTANCE);
        converters.put(Long.class, LongConverter.INSTANCE);
        converters.put(Long.TYPE, LongConverter.INSTANCE);

        converters.put(Date.class, DateConverter.INSTANCE);
        converters.put(Class.class, ClassConverter.INSTANCE);

    }

    /**
     * De-register all converters, default one and additional registered ones. Should be executed after each test where a
     * specific {@link Converter} is registered which could interfere with the normal execution of other tests.
     */
    public static void deregisterAllConverters() {
        converters.clear();
    }

    private static Type getTypeOfConverter(Class clazz) {
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

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        if (!configValues.containsKey(propertyName)) {
            throw new NoSuchElementException(String.format("Key %s does not exists", propertyName));
        }
        String value = configValues.get(propertyName);
        return convert(value, propertyType);
    }

    @Override
    public ConfigValue getConfigValue(String propertyName) {
        return null;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        String value = configValues.get(propertyName);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(getValue(propertyName, propertyType));
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return null;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return null;
    }

    public <T> T convert(String value, Class<T> asType) {
        if (value != null) {
            Optional<Converter<T>> converter = getConverter(asType);
            if (converter.isPresent()) {
                return converter.get().convert(value);
            } else {
                throw new IllegalArgumentException("No Converter registered for class " + asType);
            }
        }

        return null;
    }

    public <T> Optional<Converter<T>> getConverter(Class<T> asType) {
        Converter<T> converter = (Converter<T>) converters.get(asType);
        if (converter == null) {
            converter = ImplicitConverter.getImplicitConverter(asType);
        }

        return Optional.of(converter);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        throw new IllegalArgumentException(String.format("unwrap to Class %s not supported", type));
    }
}
