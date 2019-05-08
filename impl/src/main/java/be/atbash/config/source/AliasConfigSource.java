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
package be.atbash.config.source;

import be.atbash.util.StringUtils;
import be.atbash.util.exception.AtbashUnexpectedException;
import be.atbash.util.resource.ResourceScanner;
import be.atbash.util.resource.ResourceUtil;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AliasConfigSource implements ConfigSource {

    private Logger logger = LoggerFactory.getLogger(AliasConfigSource.class);

    private Map<String, String> keyMapping;
    private List<String> usedOldKeys;

    public AliasConfigSource() {
        usedOldKeys = new ArrayList<>();
        Set<String> resources = ResourceScanner.getInstance().getResources("config/alias.*.properties");

        keyMapping = new HashMap<>();

        for (String resource : resources) {
            keyMapping.putAll(readMapping(resource));
        }
    }

    private Map<String, String> readMapping(String resource) {
        Map<String, String> result = new HashMap<>();
        try {
            InputStream stream = ResourceUtil.getInstance().getStream(ResourceUtil.CLASSPATH_PREFIX + resource);
            Properties properties = new Properties();
            properties.load(stream);
            stream.close();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (IOException e) {
            throw new AtbashUnexpectedException(e);
        }
        return result;
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>(); // There are no real properties kept here, just a mapping.
    }

    @Override
    public Set<String> getPropertyNames() {
        return new HashSet<>(); // There are no real properties kept here, just a mapping.
    }

    @Override
    public String getValue(String propertyName) {
        String result = null;
        if (keyMapping.containsKey(propertyName)) {
            // getOptionalValue so that we doin't get an Exception with old key doesn't exists.
            String oldName = keyMapping.get(propertyName);

            result = ConfigProvider.getConfig().getOptionalValue(oldName, String.class).orElse(null);

            if (StringUtils.hasText(result)) {
                if (!usedOldKeys.contains(oldName)) {
                    if (logger.isInfoEnabled()) {
                        logger.info(String.format("Found a configuration value for deprecated key '%s'. Please use the new key '%s'", oldName, propertyName));
                    }
                    usedOldKeys.add(oldName);
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Alias config Source";
    }

    @Override
    public int getOrdinal() {
        return Integer.MIN_VALUE;  // Make sure we are always the last one.
    }
}
