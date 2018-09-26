/*
 * Copyright 2017-2018 Rudy De Busscher
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
package be.atbash.config.converter;

import be.atbash.config.test.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class AtbashDateConverterTest {

    private AtbashDateConverter converter;

    @Before
    public void setup() {
        converter = new AtbashDateConverter();
    }

    @After
    public void cleanup() {
        TestConfig.resetConfig();
    }

    @Test
    public void convert_defaultFormat() {
        Date result = converter.convert("2017-11-15");

        assertThat(result).withDateFormat("dd-MM-yyyy").isEqualTo("15-11-2017");
    }

    @Test
    public void convert_inlineFormat() {
        Date result = converter.convert("11-2017-15,MM-yyyy-dd");

        assertThat(result).withDateFormat("dd-MM-yyyy").isEqualTo("15-11-2017");
    }

    @Test
    public void convert_configBasedFormat() {

        TestConfig.registerDefaultConverters();
        TestConfig.addConfigValue("atbash.date.pattern", "dd-yyyy-MM");

        Date result = converter.convert("15-2017-11");

        assertThat(result).withDateFormat("dd-MM-yyyy").isEqualTo("15-11-2017");
    }

}