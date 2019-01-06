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
package be.atbash.config.examples.se.logging;

import be.atbash.config.logging.ConfigEntry;
import be.atbash.config.logging.ModuleConfig;

/**
 *
 */

public class ModuleConfiguration implements ModuleConfig {

    @ConfigEntry
    public String defineStringConfigValue() {
        return "Config Value";
    }

    @ConfigEntry
    public Long defineLongConfigValue() {
        return 123L;
    }

    @ConfigEntry
    public String canHandleNull() {
        return null;
    }

    @ConfigEntry
    public String needWarningInLog(Long parameter) {
        return null;
    }

    @ConfigEntry
    public void needWarningInLogNoReturn() {
        // Test method which doesn't need to do anything.
    }

    public String doNotLog() {
        return "Should never be in log";
    }

    @ConfigEntry("Config With parameter")
    public Long getConfigValueWithParameter(String code) {
        return null;
    }

}
