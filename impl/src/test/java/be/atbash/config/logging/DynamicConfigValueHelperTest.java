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
package be.atbash.config.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicConfigValueHelperTest {

    private DynamicConfigValueHelper dynamicConfigValueHelper = new DynamicConfigValueHelper();

    @Test
    void getTruncatedConfigValue_stdCase() {
        assertThat(dynamicConfigValueHelper.getTruncatedConfigValue("{5}12345678")).isEqualTo("12345");
    }

    @Test
    void getTruncatedConfigValue_LongerThenLenght() {
        assertThat(dynamicConfigValueHelper.getTruncatedConfigValue("{7}1234")).isEqualTo("1234");
    }

    @Test
    void getTruncatedConfigValue_NoTruncationInfo() {
        assertThat(dynamicConfigValueHelper.getTruncatedConfigValue("123456")).isEqualTo("123456");
    }

    @Test
    void getTruncatedConfigValue_CompleteTruncation() {
        assertThat(dynamicConfigValueHelper.getTruncatedConfigValue("{0}123456")).isEqualTo("[Dynamic value]");
    }

    @Test
    void getCompleteConfigValue_LongerThenLenght() {
        assertThat(dynamicConfigValueHelper.getCompleteConfigValue("{7}1234")).isEqualTo("1234");
    }

    @Test
    void getCompleteConfigValue_NoTruncationInfo() {
        assertThat(dynamicConfigValueHelper.getCompleteConfigValue("123")).isEqualTo("123");
    }

    @Test
    void getCompleteConfigValue_CompleteTruncation() {
        assertThat(dynamicConfigValueHelper.getCompleteConfigValue("{0}123456")).isEqualTo("123456");

    }

}