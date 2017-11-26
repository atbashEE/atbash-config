/**
 * Copyright 2017 Rudy De Busscher
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
package be.atbash.config

import be.atbash.config.converter.AtbashDateConverter
import org.eclipse.microprofile.config.Config
import org.eclipse.microprofile.config.spi.ConfigSource

/**
 *
 */

class TestConfig implements Config {

    static Map<String, String> configValues = new HashMap<>()

    static addConfigValue(String key, String value) {
        configValues.put(key, value)
    }

    static resetConfig() {
        configValues.clear()
    }

    @Override
     <T> T getValue(String propertyName, Class<T> propertyType) {
        if (!configValues.containsKey(propertyName)) {
            throw new NoSuchElementException("Key ${propertyName} does not exists")
        }
        String value = configValues.get(propertyName)
        T result
        if (propertyType == Date) {
            result = new AtbashDateConverter().convert(value)
        } else {
            result = value
        }
        return result
    }

    @Override
     <T> T getOptionalValue(String propertyName, Class<T> propertyType) {
        String value = configValues.get(propertyName)
        if (value == null) {
            return null
        }
        return getValue(propertyName, propertyType)
    }

    @Override
    Iterable<String> getPropertyNames() {
        return null
    }

    @Override
    Iterable<ConfigSource> getConfigSources() {
        return null
    }
}
