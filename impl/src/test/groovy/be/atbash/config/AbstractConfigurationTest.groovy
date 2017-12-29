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

import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import spock.lang.Specification

/**
 *
 */

class AbstractConfigurationTest extends Specification {

    @Subject
    AbstractConfiguration config = new AbstractConfiguration() {}

    def cleanup() {
        TestConfig.resetConfig()
    }

    def "GetOptionalValue"() {
        given:
        TestConfig.addConfigValue("someConfig", "configValue")

        when:
        String notOptionalValue = config.getOptionalValue("someConfig", "ValueNotUsed", String)
        String optionalValue = config.getOptionalValue("nonExisting", "defaultValue", String)

        then:
        notOptionalValue == "configValue"
        optionalValue == "defaultValue"

    }
}