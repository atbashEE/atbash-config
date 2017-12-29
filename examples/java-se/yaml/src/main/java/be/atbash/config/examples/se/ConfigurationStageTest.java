/*
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
package be.atbash.config.examples.se;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Date;

/**
 *
 */

public class ConfigurationStageTest {

    public static void main(String[] args) {
        System.setProperty("S", "test");

        Config config = ConfigProvider.getConfig();

        System.out.println("value1=" + config.getValue("value1", String.class));

        System.out.println("level.deeper.value2=" + config.getValue("level.deeper.value2", String.class));

        System.out.println("dateValue=" + config.getValue("dateValue", Date.class));

        System.out.println("test.only=" + config.getOptionalValue("test.only", String.class));

        System.out.println("nonExisting=" + config.getOptionalValue("nonExisting", String.class));

        //System.out.println("nonExisting="+config.getValue("nonExisting", String.class)); ;
    }
}
