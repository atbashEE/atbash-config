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
package be.atbash.config.examples.ee;

import be.atbash.config.logging.ConfigEntry;
import be.atbash.config.logging.ModuleConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 *
 */
@ApplicationScoped
public class ApplicationConfiguration implements ModuleConfig {

    @Inject
    private Config config;

    @Inject
    @ConfigProperty(name = "value1")
    private String value1;

    @Inject
    @ConfigProperty(name = "testOnly", defaultValue = "Not within Test")
    private String testOnly;

    @ConfigEntry
    public Integer getValue2() {
        return config.getValue("value2", Integer.class);
    }

    @ConfigEntry
    public String getValue1() {
        return value1;
    }

    @ConfigEntry
    public String getTestOnly() {
        return testOnly;
    }
}
