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

import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */

class DynamicConfigValueHelperTest extends Specification {

    @Shared
    DynamicConfigValueHelper dynamicConfigValueHelper = new DynamicConfigValueHelper()


    def getTruncatedConfigValue_stdCase() {
        expect:
        dynamicConfigValueHelper.getTruncatedConfigValue("{5}12345678") == "12345"
    }

    def getTruncatedConfigValue_LongerThenLenght() {
        expect:
        dynamicConfigValueHelper.getTruncatedConfigValue("{7}1234") == "1234"
    }

    def getTruncatedConfigValue_NoTruncationInfo() {
        expect:
        dynamicConfigValueHelper.getTruncatedConfigValue("123456") == "123456"
    }

    def getTruncatedConfigValue_CompleteTruncation() {
        expect:
        dynamicConfigValueHelper.getTruncatedConfigValue("{0}123456") == "[Dynamic value]"
    }

    def getCompleteConfigValue_LongerThenLenght() {
        expect:
        dynamicConfigValueHelper.getCompleteConfigValue("{7}1234") == "1234"
    }

    def getCompleteConfigValue_NoTruncationInfo() {
        expect:
        dynamicConfigValueHelper.getCompleteConfigValue("123") == "123"
    }

    def getCompleteConfigValue_CompleteTruncation() {
        expect:
        dynamicConfigValueHelper.getCompleteConfigValue("{0}123456") == "123456"

    }
}
