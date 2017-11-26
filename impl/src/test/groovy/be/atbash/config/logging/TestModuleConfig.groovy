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
package be.atbash.config.logging

/**
 *
 */

class TestModuleConfig implements ModuleConfig {

    @ConfigEntry
    String defineStringConfigValue() {
        return "Config Value"
    }

    @ConfigEntry
    Long defineLongConfigValue() {
        return 123L
    }

    @ConfigEntry
    MyConfigObject defineComplexConfig() {
        return new MyConfigObject()
    }

    @ConfigEntry
    String canHandleNull() {
        return null
    }

    @ConfigEntry
    String needWarningInLog(Long parameter) {
        return null
    }

    @ConfigEntry
    void needWarningInLogNoReturn() {
    }

    String doNotLog() {
        return "Should never be in log"
    }

    @ConfigEntry("Config With parameter")
    Long getConfigValueWithParameter(String code) {
        return null
    }

    @ConfigEntry(classResult = MyConfigObject.class)
    MyConfigObject getClassResultConfigValue() {
        return null

    }

    static class NoLogConfig implements ModuleConfig {
        @ConfigEntry(noLogging = true)
        String defineConfigValue() {
            return "Secret Value"
        }

        @ConfigEntry(noLogging = true)
        Long defineNullConfigValue() {
            return null
        }

    }

    static class MyConfigObject {
        @Override
        String toString() {
            final StringBuilder sb = new StringBuilder("MyConfigObject{");
            sb.append('}')
            return sb.toString()
        }
    }
}