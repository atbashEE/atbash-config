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
package be.atbash.config.spi;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Set;

/**
 *
 */

public abstract class AbstractConfigSource implements ConfigSource {

    /**
     * Gets all property names known to this config source, without evaluating the values.
     * <p>
     * For backwards compatibility, there is a default implementation that just returns the keys of {@code getProperties()}
     * slower ConfigSource implementations should replace this with a more performant implementation
     *
     * @return the set of property keys that are known to this ConfigSource
     */
    @Override
    public Set<String> getPropertyNames() {
        return getProperties().keySet();
    }

    /**
     * Return the ordinal for this config source. If a property is specified in multiple config sources, the value
     * in the config source with the highest ordinal takes precedence.
     * For the config sources with the same ordinal value, the config source names will
     * be used for sorting according to string sorting criteria.
     * Note that this property only gets evaluated during ConfigSource discovery.
     * <p>
     * The default ordinals for the default config sources:
     * <ol>
     * <li>System properties (default ordinal=400)</li>
     * <li>Environment properties (default ordinal=300)</li>
     * <li>/META-INF/microprofile-config.properties (default ordinal=100)</li>
     * </ol>
     * <p>
     * <p>
     * Any ConfigSource part of an application will typically use an ordinal between 0 and 200.
     * ConfigSource provided by the container or 'environment' typically use an ordinal higher than 200.
     * A framework which intends have values overwritten by the application will use ordinals between 0 and 100.
     * The property "config_ordinal" can be specified to override the default value.
     *
     * @return the ordinal value
     */
    @Override
    public int getOrdinal() {
        String configOrdinal = getValue(CONFIG_ORDINAL);
        if (configOrdinal != null) {
            try {
                return Integer.parseInt(configOrdinal);
            } catch (NumberFormatException ignored) {
                // Ignored
            }
        }
        return 100; // DEFAULT_VALUE
    }

}
