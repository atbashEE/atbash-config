/*
 * Copyright 2017-2020 Rudy De Busscher
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

import be.atbash.config.test.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractConfigurationTest {

    private AbstractConfiguration config = new AbstractConfiguration() {
    };

    @AfterEach
    public void cleanup() {
        TestConfig.resetConfig();
    }

    @Test
    public void getOptionalValue() {
        TestConfig.registerDefaultConverters();
        TestConfig.addConfigValue("someConfig", "configValue");

        String notOptionalValue = config.getOptionalValue("someConfig", "ValueNotUsed", String.class);
        String optionalValue = config.getOptionalValue("nonExisting", "defaultValue", String.class);

        assertThat(notOptionalValue).isEqualTo("configValue");
        assertThat(optionalValue).isEqualTo("defaultValue");

    }
}
