/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.config.configsource;

import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ConfigSource} which uses {@link System#getenv()}
 * <p>
 * We also allow to write underlines _ instead of dots _ in the
 * environment via export (unix) or SET (windows)
 *
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Typed
@Vetoed
public class SystemEnvConfigSource extends BaseConfigSource {
    private Map<String, String> configValues;
    private Map<String, String> uppercasePosixValues;

    public SystemEnvConfigSource() {
        uppercasePosixValues = new HashMap<>();
        configValues = System.getenv();
        initOrdinal(300);

        for (Map.Entry<String, String> e : configValues.entrySet()) {
            String originalKey = e.getKey();
            String posixKey = replaceNonPosixEnvChars(originalKey).toUpperCase();
            if (!originalKey.equals(posixKey)) {
                uppercasePosixValues.put(posixKey, e.getValue());
            }
        }
    }

    @Override
    public String getName() {
        return "system_env";
    }

    @Override
    public Map<String, String> getProperties() {
        return configValues;
    }

    @Override
    public String getValue(String key) {
        String val = configValues.get(key);
        if (val == null) {
            key = replaceNonPosixEnvChars(key);
            val = configValues.get(key);
        }
        if (val == null) {
            key = key.toUpperCase();
            val = configValues.get(key);
        }
        if (val == null) {
            val = uppercasePosixValues.get(key);
        }

        return val;
    }

    private String replaceNonPosixEnvChars(String key) {
        return key.replaceAll("[^A-Za-z0-9]", "_");
    }
}
