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
package be.atbash.config.test.converters;

import org.eclipse.microprofile.config.spi.Converter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A Converter factory + impl for 'common sense converters'
 */
public abstract class ImplicitConverter {

    private ImplicitConverter() {
    }

    public static <T> Converter<T> getImplicitConverter(Class<T> clazz) {
        // handle ct with String param
        Converter<T> converter = hasConverterMethod(clazz, "of", String.class);
        if (converter == null) {
            converter = hasConverterMethod(clazz, "of", CharSequence.class);
        }
        if (converter == null) {
            converter = hasConverterMethod(clazz, "valueOf", String.class);
        }
        if (converter == null) {
            converter = hasConverterMethod(clazz, "valueOf", CharSequence.class);
        }
        if (converter == null) {
            converter = hasConverterCt(clazz, String.class);
        }
        if (converter == null) {
            converter = hasConverterCt(clazz, CharSequence.class);
        }
        if (converter == null) {
            converter = hasConverterMethod(clazz, "parse", String.class);
        }
        if (converter == null) {
            converter = hasConverterMethod(clazz, "parse", CharSequence.class);
        }
        return converter;
    }

    private static Converter hasConverterCt(Class<?> clazz, Class<?> paramType) {
        try {
            final Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(paramType);
            if (!declaredConstructor.isAccessible()) {
                declaredConstructor.setAccessible(true);
            }
            return value -> {
                try {
                    return declaredConstructor.newInstance(value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            };
        } catch (NoSuchMethodException e) {
            // all fine
        }
        return null;
    }

    private static <T> Converter<T> hasConverterMethod(Class<T> clazz, String methodName, Class<?> paramType) {
        // handle valueOf with CharSequence param
        try {
            final Method method = clazz.getDeclaredMethod(methodName, paramType);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (Modifier.isStatic(method.getModifiers()) && method.getReturnType().equals(clazz)) {
                return value -> {
                    try {
                        return (T) method.invoke(null, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Error while converting the value " + value +
                                " to type " + method.getReturnType());
                    }
                };
            }
        } catch (NoSuchMethodException e) {
            // all fine
        }
        return null;
    }

    public static class ImplicitArrayConverter<T> implements Converter<T> {
        private final Converter<T> converter;
        private final Class<?> type;

        public ImplicitArrayConverter(Converter<T> converter, Class<T> type) {
            this.converter = converter;
            this.type = type;
        }

        @Override
        public T convert(String valueStr) {
            if (valueStr == null) {
                return null;
            }

            List list = new ArrayList();
            StringBuilder currentValue = new StringBuilder();
            int length = valueStr.length();
            for (int i = 0; i < length; i++) {
                char c = valueStr.charAt(i);
                if (c == '\\') {
                    if (i < length - 1) {
                        char nextC = valueStr.charAt(i + 1);
                        currentValue.append(nextC);
                        i++;
                    }
                } else if (c == ',') {
                    String trimedVal = currentValue.toString().trim();
                    if (trimedVal.length() > 0) {
                        list.add(converter.convert(trimedVal));
                    }

                    currentValue.setLength(0);
                } else {
                    currentValue.append(c);
                }
            }

            String trimedVal = currentValue.toString().trim();
            if (trimedVal.length() > 0) {
                list.add(converter.convert(trimedVal));
            }

            // everything else is an Object array
            Object array = Array.newInstance(type, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return (T) array;
        }

    }
}
