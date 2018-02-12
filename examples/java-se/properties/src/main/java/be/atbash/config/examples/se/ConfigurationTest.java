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
package be.atbash.config.examples.se;

import be.atbash.config.examples.se.testclasses.ConvTestTypeWStringCt;
import be.atbash.config.examples.se.testclasses.ConvTestTypeWStringValueOf;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Date;

/**
 *
 */

public class ConfigurationTest {

    public static void main(String[] args) {
        Config config = ConfigProvider.getConfig();

        System.out.println("value1=" + config.getValue("value1", String.class));
        System.out.println("value2=" + config.getValue("value2", Integer.class));
        System.out.println("value3=" + config.getValue("value3", Boolean.class));
        System.out.println("dateValue=" + config.getValue("dateValue", Date.class));
        System.out.println("nonExisting=" + config.getOptionalValue("nonExisting", String.class));
        System.out.println("testOnly=" + config.getOptionalValue("testOnly", String.class));
        //System.out.println("nonExisting="+config.getValue("nonExisting", String.class));

        ConvTestTypeWStringCt value = config.getValue("valueForClass", ConvTestTypeWStringCt.class);
        System.out.println("Class with Constructor having String parameter : " + value.getVal());

        ConvTestTypeWStringValueOf value2 = config.getValue("valueForClass", ConvTestTypeWStringValueOf.class);
        System.out.println("Class with static valueOf Method : " + value2.getVal());

        Class<?> aClass = config.getValue("classname", Class.class);
        System.out.println(aClass.getName());
    }
}
