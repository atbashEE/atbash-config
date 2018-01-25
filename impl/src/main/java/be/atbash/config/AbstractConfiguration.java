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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */

public abstract class AbstractConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        T result = ConfigOptionalValue.getValue(propertyName, propertyType);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    protected <T> T getOptionalValue(String propertyName, Class<T> propertyType) {
        return getOptionalValue(propertyName, null, propertyType);
    }
}
