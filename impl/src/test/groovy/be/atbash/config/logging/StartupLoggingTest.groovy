/**
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
package be.atbash.config.logging

import be.atbash.config.AbstractConfiguration
import be.atbash.config.logging.testclasses.HierarchyConfig
import be.atbash.config.logging.testclasses.TestModuleConfig
import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import spock.lang.Specification

import java.lang.reflect.Field

/**
 *
 */

class StartupLoggingTest extends Specification {

    private static final String VALUE_SEPARATOR = "value:"
    private static final String NO_LOGGING = "Nologgingparameteractive"

    @Collaborator
    DynamicConfigValueHelper valueHelper = new DynamicConfigValueHelper()

    @Subject
    StartupLogging logging

    def getConfigInfo() {
        given:
        System.setProperty("atbash.config.log.all", "")

        when:
        String info = logging.getConfigInfo(new TestModuleConfig())
        String data = info.replaceAll("\\s", "")

        then:
        data.contains("defineStringConfigValue" + VALUE_SEPARATOR + "ConfigValue")
        data.contains("defineLongConfigValue" + VALUE_SEPARATOR + "123")
        data.contains("defineComplexConfig" + VALUE_SEPARATOR + "MyConfigObject{}")
        data.contains("canHandleNull" + VALUE_SEPARATOR + "null")
        data.contains("needWarningInLog" + VALUE_SEPARATOR + "unknown-Methodhasaparameter")
        data.contains("needWarningInLogNoReturn" + VALUE_SEPARATOR + "unknown-Methodhasnoreturnvalue")
        !data.contains("doNotLog")
        !data.contains("Shouldneverbeinlog")
        data.contains("getConfigValueWithParameter" + VALUE_SEPARATOR + "[ConfigWithparameter]")
        data.contains("getClassResultConfigValue" + VALUE_SEPARATOR + "be.atbash.config.logging.testclasses.TestModuleConfig.MyConfigObject")
    }

    def getConfigInfo_NoLogging() {
        given:
        System.setProperty("atbash.config.log.all", "")

        when:
        String info = logging.getConfigInfo(new TestModuleConfig.NoLogConfig())
        String data = info.replaceAll("\\s", "")

        then:
        data.contains("defineConfigValue" + VALUE_SEPARATOR + NO_LOGGING + "[nonnullvalue]")
        data.contains("defineNullConfigValue" + VALUE_SEPARATOR + NO_LOGGING + "null")

    }

    def getConfigInfo_AllLogging() throws NoSuchFieldException, IllegalAccessException {
        given:
        System.setProperty("atbash.config.log.all", "tRue")

        Field allLoggingActivated = logging.getClass().getDeclaredField("allLoggingActivated")
        allLoggingActivated.setAccessible(true)
        allLoggingActivated.setBoolean(logging, Boolean.TRUE)

        when:
        String info = logging.getConfigInfo(new TestModuleConfig.NoLogConfig())
        String data = info.replaceAll("\\s", "")

        then:

        data.contains("defineConfigValue" + VALUE_SEPARATOR + "SecretValue")
        !data.contains("defineNullConfigValue" + VALUE_SEPARATOR + NO_LOGGING)

    }

    def getConfigInfo_hierarchy() {
        given:
        System.setProperty("atbash.config.log.all", "")

        when:
        String info = logging.getConfigInfo(new HierarchyConfig())
        String data = info.replaceAll("\\s", "")

        then:
        data.contains(HierarchyConfig.name)
        data.contains("defineStringConfigValue" + VALUE_SEPARATOR + "ConfigValue")
        !data.contains(AbstractConfiguration.name)
    }

    def getConfigInfo_disabledLogging() {
        given:
        System.setProperty("atbash.config.log.all", "")
        System.setProperty("atbash.config.log.disabled", "true")

        Field loggingDisabled = logging.getClass().getDeclaredField("loggingDisabled")
        loggingDisabled.setAccessible(true)
        loggingDisabled.setBoolean(logging, Boolean.TRUE)

        when:
        String info = logging.getConfigInfo(new TestModuleConfig())

        then:
        info == null
    }
}
