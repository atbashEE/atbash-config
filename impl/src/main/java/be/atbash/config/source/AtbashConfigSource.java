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
package be.atbash.config.source;

import be.atbash.config.spi.AbstractConfigSource;
import be.atbash.config.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 */

public class AtbashConfigSource extends AbstractConfigSource {

    private static final Logger LOG = LoggerFactory.getLogger(AtbashConfigSource.class);

    private int ordinal;
    private String configLocation;

    private Map<String, String> properties;

    public AtbashConfigSource(ConfigType configType, String configLocation, int ordinal) {
        this.ordinal = ordinal;
        this.configLocation = configLocation;
        try {
            InputStream inputStream = ResourceUtils.getInputStream(configLocation);

            switch (configType) {

                case PROPERTIES:
                    properties = handlePropertiesFile(inputStream);
                    break;
                case YAML:
                    properties = handleYAMLFile(inputStream);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("ConfigType %s not supported!", configType));
            }

            inputStream.close();

        } catch (IOException e) {
            throw new ConfigurationLoadingException(e.getMessage());
        }

    }

    private Map<String, String> handleYAMLFile(InputStream inputStream) {
        String data = convertStreamToString(inputStream);
        Yaml yaml = new Yaml();
        Map<String, Object> propertiesMap = yaml.load(data);

        Map<String, Object> values = new HashMap<>();
        readConfigValues("", propertiesMap, values);

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof List) {
                List<?> listEntry = (List<?>) entry.getValue();
                if (listEntry.get(0) instanceof String) {
                    result.put(entry.getKey(), assembleStringList(listEntry));
                }
                // We can't support this type of properties constructs
                LOG.warn(String.format("No support for List of type %s, found for key %s within YAML configuration file",
                        listEntry.get(0).getClass().getName(), entry.getKey()));
            } else {
                if (entry.getValue() == null) {
                    LOG.warn(String.format("Empty value for parameter key %s", entry.getKey()));
                } else {
                    result.put(entry.getKey(), entry.getValue().toString());
                }

            }
        }
        return result;
    }

    private String assembleStringList(List<?> listEntry) {
        StringBuilder result = new StringBuilder();
        for (Object item : listEntry) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(item.toString());
        }
        return result.toString();
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static void readConfigValues(String prefix, Map<String, Object> load, Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : load.entrySet()) {
            String key = prefix.length() == 0 ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = load.get(entry.getKey());

            if (!(value instanceof Map)) {
                values.put(key, value);
            } else {
                readConfigValues(key, (Map<String, Object>) value, values);
            }
        }
    }

    private Map<String, String> handlePropertiesFile(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ConfigurationLoadingException(e.getMessage());
        }

        Map<String, String> result = new HashMap<>();

        for (final String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name));
        }
        return result;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return String.format("Application configuration [%s]", configLocation);
    }

    @Override
    public int getOrdinal() {
        return ordinal;
    }
}
