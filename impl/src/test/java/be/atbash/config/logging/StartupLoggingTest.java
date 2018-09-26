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
package be.atbash.config.logging;

import be.atbash.config.AbstractConfiguration;
import be.atbash.config.logging.testclasses.HierarchyConfig;
import be.atbash.config.logging.testclasses.TestModuleConfig;
import be.atbash.util.TestReflectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupLoggingTest {

    private static final String VALUE_SEPARATOR = "value:";
    private static final String NO_LOGGING = "Nologgingparameteractive";

    private DynamicConfigValueHelper valueHelper;

    private StartupLogging logging;

    @Before
    public void setup() throws IllegalAccessException {
        valueHelper = new DynamicConfigValueHelper();
        logging = new StartupLogging();

        TestReflectionUtils.injectDependencies(logging, valueHelper);
    }

    @Test
    public void getConfigInfo() {
        System.setProperty("atbash.config.log.all", "");

        String info = logging.getConfigInfo(new TestModuleConfig());
        String data = info.replaceAll("\\s", "");


        assertThat(data).contains("defineStringConfigValue" + VALUE_SEPARATOR + "ConfigValue");
        assertThat(data).contains("defineLongConfigValue" + VALUE_SEPARATOR + "123");
        assertThat(data).contains("defineComplexConfig" + VALUE_SEPARATOR + "MyConfigObject{}");
        assertThat(data).contains("canHandleNull" + VALUE_SEPARATOR + "null");
        assertThat(data).contains("needWarningInLog" + VALUE_SEPARATOR + "unknown-Methodhasaparameter");
        assertThat(data).contains("needWarningInLogNoReturn" + VALUE_SEPARATOR + "unknown-Methodhasnoreturnvalue");
        assertThat(data).doesNotContain("doNotLog");
        assertThat(data).doesNotContain("Shouldneverbeinlog");
        assertThat(data).contains("getConfigValueWithParameter" + VALUE_SEPARATOR + "[ConfigWithparameter]");
        assertThat(data).contains("getClassResultConfigValue" + VALUE_SEPARATOR + "be.atbash.config.logging.testclasses.TestModuleConfig.MyConfigObject");
    }

    @Test
    public void getConfigInfo_NoLogging() {
        System.setProperty("atbash.config.log.all", "");

        String info = logging.getConfigInfo(new TestModuleConfig.NoLogConfig());
        String data = info.replaceAll("\\s", "");

        assertThat(data).contains("defineConfigValue" + VALUE_SEPARATOR + NO_LOGGING + "[nonnullvalue]");
        assertThat(data).contains("defineNullConfigValue" + VALUE_SEPARATOR + NO_LOGGING + "null");

    }

    @Test
    public void getConfigInfo_AllLogging() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("atbash.config.log.all", "tRue");

        Field allLoggingActivated = logging.getClass().getDeclaredField("allLoggingActivated");
        allLoggingActivated.setAccessible(true);
        allLoggingActivated.setBoolean(logging, Boolean.TRUE);

        String info = logging.getConfigInfo(new TestModuleConfig.NoLogConfig());
        String data = info.replaceAll("\\s", "");


        assertThat(data).contains("defineConfigValue" + VALUE_SEPARATOR + "SecretValue");
        assertThat(data).doesNotContain("defineNullConfigValue" + VALUE_SEPARATOR + NO_LOGGING);

    }

    @Test
    public void getConfigInfo_hierarchy() {
        System.setProperty("atbash.config.log.all", "");

        String info = logging.getConfigInfo(new HierarchyConfig());
        String data = info.replaceAll("\\s", "");

        assertThat(data).contains(HierarchyConfig.class.getName());
        assertThat(data).contains("defineStringConfigValue" + VALUE_SEPARATOR + "ConfigValue");
        assertThat(data).doesNotContain(AbstractConfiguration.class.getName());
    }

    @Test
    public void getConfigInfo_disabledLogging() throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("atbash.config.log.all", "");
        System.setProperty("atbash.config.log.disabled", "true");

        Field loggingDisabled = logging.getClass().getDeclaredField("loggingDisabled");
        loggingDisabled.setAccessible(true);
        loggingDisabled.setBoolean(logging, Boolean.TRUE);

        String info = logging.getConfigInfo(new TestModuleConfig());

        assertThat(info).isNull();
    }

}
