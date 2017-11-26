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
package be.atbash.config.converter

import be.atbash.config.TestConfig
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import spock.lang.Specification

/**
 *
 */

class AtbashDateConverterTest extends Specification {

    @Subject
    AtbashDateConverter converter

    def Convert_defaultFormat() {
        when:
        Date result = converter.convert("2017-11-15")

        then:
        "15-11-2017" == result.format('dd-MM-yyyy')
    }

    def Convert_inlineFormat() {
        when:
        Date result = converter.convert("11-2017-15,MM-yyyy-dd")

        then:
        "15-11-2017" == result.format('dd-MM-yyyy')
    }

    def Convert_configBasedFormat() {
        given:
        TestConfig.addConfigValue("atbash.date.pattern", "dd-yyyy-MM")

        when:
        Date result = converter.convert("15-2017-11")

        then:
        "15-11-2017" == result.format('dd-MM-yyyy')
    }
}
