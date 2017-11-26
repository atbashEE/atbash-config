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

import org.eclipse.microprofile.config.Config
import org.eclipse.microprofile.config.spi.ConfigBuilder
import org.eclipse.microprofile.config.spi.ConfigProviderResolver

/**
 *
 */

class TestConfigProvider extends ConfigProviderResolver {
    @Override
    Config getConfig() {
        return new TestConfig()
    }

    @Override
    Config getConfig(ClassLoader loader) {
        return null
    }

    @Override
    ConfigBuilder getBuilder() {
        return null
    }

    @Override
    void registerConfig(Config config, ClassLoader classLoader) {

    }

    @Override
    void releaseConfig(Config config) {

    }
}
