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

import be.atbash.config.source.AtbashConfigSource;
import be.atbash.config.source.ConfigType;
import be.atbash.util.StringUtils;
import be.atbash.util.resource.ResourceUtil;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.util.*;

import static be.atbash.util.resource.ResourceUtil.CLASSPATH_PREFIX;


/**
 *
 */

public class ApplicationConfigSourceProvider implements ConfigSourceProvider {

    private final Set<String> baseConfigurationNames;

    public ApplicationConfigSourceProvider() {
        baseConfigurationNames = new HashSet<>();
        // Use classloader of this class explicitly, otherwise serviceloader files defined in the war aren't found.
        for (BaseConfigurationName baseConfigurationName : ServiceLoader.load(BaseConfigurationName.class
                , ApplicationConfigSourceProvider.class.getClassLoader())) {
            baseConfigurationNames.add(baseConfigurationName.getBase());
        }
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        if (baseConfigurationNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConfigSource> result = new ArrayList<>();

        for (String configurationName : baseConfigurationNames) {

            for (ConfigType configType : ConfigType.values()) {

                String configLocation = CLASSPATH_PREFIX + configurationName + configType.getSuffix();
                if (ResourceUtil.getInstance().resourceExists(configLocation)) {
                    result.add(new AtbashConfigSource(configType, configLocation, 150));
                }
            }

            String stageValue = System.getProperty("S");
            if (StringUtils.hasText(stageValue)) {
                for (ConfigType configType : ConfigType.values()) {

                    String configLocation = CLASSPATH_PREFIX + configurationName + "-" + stageValue + configType.getSuffix();
                    if (ResourceUtil.getInstance().resourceExists(configLocation)) {
                        result.add(new AtbashConfigSource(configType, configLocation, 200));
                    }
                }

            }

            String externalFile = System.getProperty("s");
            if (StringUtils.hasText(externalFile)) {
                ConfigType configType = defineConfigType(externalFile);
                if (ResourceUtil.getInstance().resourceExists(externalFile)) {
                    result.add(new AtbashConfigSource(configType, externalFile, 250));
                }
            }
        }

        return result;
    }

    private ConfigType defineConfigType(String externalFile) {
        ConfigType result = null;

        int i = externalFile.lastIndexOf('.');
        String suffix = null;
        if (i > 0) {
            suffix = externalFile.substring(i);
        }
        if (!StringUtils.hasText(suffix)) {
            return ConfigType.PROPERTIES; // No suffix, so assume it is a properties file.
        }

        for (ConfigType configType : ConfigType.values()) {
            if (configType.getSuffix().equalsIgnoreCase(suffix)) {
                result = configType;
            }
        }

        if (result == null) {
            result = ConfigType.PROPERTIES; // No known suffix, assume it is a properties file.
        }

        return result;
    }
}
