/*
 * Copyright 2017-2019 Rudy De Busscher
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

import be.atbash.config.logging.ModuleConfig;
import be.atbash.util.PublicAPI;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Can be used as parent class of Configuration classes (implementing {@link ModuleConfig}.
 */
@PublicAPI
public abstract class AbstractConfiguration {

    /**
     * Reads the configuration property as an optional value, so it is not required to have a value for the key/propertyName, and
     * returns the <code>defaultValue</code> when the value isn't defined.
     *
     * @param <T>          the property type
     * @param propertyName The configuration propertyName.
     * @param propertyType The type into which the resolve property value should be converted
     * @return the resolved property value as an value of the requested type. (defaultValue when not found)
     */
    protected <T> T getOptionalValue(String propertyName, T defaultValue, Class<T> propertyType) {

        T result = ConfigProvider.getConfig().getOptionalValue(propertyName, propertyType).orElse(null);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    protected <T> T getOptionalValue(String propertyName, Class<T> propertyType) {
        return getOptionalValue(propertyName, null, propertyType);
    }
}
