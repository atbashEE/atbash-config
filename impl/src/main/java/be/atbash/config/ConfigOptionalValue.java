/*
 * Copyright 2017-2018 Rudy De Busscher
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
package be.atbash.config;

import be.atbash.config.exception.UnexpectedException;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * MP Config is using {@code Optional} as return type for the getOptionalValue(). But since we are compiling for Java 7
 * there is a UnknownMethod Exception. This class uses reflection to see if we are running Java 8 MP Config version
 * and is capable of calling the getOptionalValue method through reflection and MethodHandles.
 */
public final class ConfigOptionalValue {

    private static final ConfigOptionalValue INSTANCE = new ConfigOptionalValue();

    // The Config to use.
    private Config config;

    // Class capable of calling getOptionalValue through reflection and MethodHandles.
    private MPConfig11JDK8 mpConfig11JDK8;

    private ConfigOptionalValue() {
        config = ConfigProvider.getConfig();
        mpConfig11JDK8 = new MPConfig11JDK8(config);
    }

    public static <T> T getValue(String key, Class<T> propertyType) {
        if (!INSTANCE.mpConfig11JDK8.isActive) {
            return INSTANCE.config.getOptionalValue(key, propertyType);
        } else {
            return INSTANCE.mpConfig11JDK8.getOptionalValue(key, propertyType);
        }
    }

    private static class MPConfig11JDK8 {

        private boolean isActive;

        private Method getOptionalValueMethod;

        private MethodHandle mh;

        MPConfig11JDK8(Config config) {
            // The return type of the getOptionalValue method (on MP Config it is Optional so that we now we are using MP Config
            // And can define a MethodHandle to the orElse method.
            Class<?> optionalValueReturnType = null;
            try {
                getOptionalValueMethod = config.getClass().getMethod("getOptionalValue", String.class, Class.class);
                // The following if is for safety
                if (getOptionalValueMethod != null) {
                    optionalValueReturnType = getOptionalValueMethod.getReturnType();
                    //When java.util.Optional we are using MP Config.
                    isActive = optionalValueReturnType.getName().endsWith("Optional");
                } else {
                    throw new UnexpectedException("getOptionalValue() method not found on Config implementation.");
                }

            } catch (NoSuchMethodException e) {
                throw new UnexpectedException(e);
            }

            if (isActive) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                try {
                    //Optional.orElse()
                    mh = lookup.findVirtual(optionalValueReturnType, "orElse",
                            MethodType.methodType(Object.class, Object.class));
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new UnexpectedException(e);
                }
            }
        }

        <T> T getOptionalValue(String key, Class<T> propertyType) {
            T result;
            try {
                // This retrieves the optional Configuration parameter as an Optional.
                Object optionalResult = getOptionalValueMethod.invoke(INSTANCE.config, key, propertyType);

                // This converts it to a null when Config Parameter was not found.
                result = (T) mh.invoke(optionalResult, null);
            } catch (Throwable throwable) {
                throw new UnexpectedException(throwable);
            }
            return result;
        }
    }
}
