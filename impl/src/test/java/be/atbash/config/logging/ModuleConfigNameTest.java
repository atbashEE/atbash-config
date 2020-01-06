/*
 * Copyright 2017-2020 Rudy De Busscher
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

import be.atbash.config.logging.testclasses.Child1Config;
import be.atbash.config.logging.testclasses.Child2Config;
import be.atbash.config.logging.testclasses.Child3Config;
import be.atbash.util.TestReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModuleConfigNameTest {

    private DynamicConfigValueHelper valueHelper;

    private StartupLogging logging;

    @BeforeEach
    public void setup() throws IllegalAccessException {
        valueHelper = new DynamicConfigValueHelper();
        logging = new StartupLogging();

        TestReflectionUtils.injectDependencies(logging, valueHelper);
    }

    @Test
    public void moduleConfigName_Scenario1() {
        // No @ModuleConfigName
        String info = logging.getConfigInfo(new Child1Config());
        String data = info.replaceAll("\\s", "");

        assertThat(data).isEqualTo("Configimplementation:be.atbash.config.logging.testclasses.Child1Configmethod:defineChild1Valuevalue:Child1ConfigValueConfigimplementation:be.atbash.config.logging.testclasses.ParentConfigmethod:defineParentValuevalue:ParentConfigValue");
    }

    @Test
    public void moduleConfigName_Scenario2() {
        // @ModuleConfigName, no className
        String info = logging.getConfigInfo(new Child2Config());
        String data = info.replaceAll("\\s", "");

        assertThat(data).isEqualTo("configNameScenario2:method:defineChild2Valuevalue:Child2ConfigValuemethod:defineParentValuevalue:ParentConfigValue");
    }

    @Test
    public void moduleConfigName_Scenario3() {
        // @ModuleConfigName, with className
        String info = logging.getConfigInfo(new Child3Config());
        String data = info.replaceAll("\\s", "");

        assertThat(data).isEqualTo("Configimplementation:configNameScenario3(be.atbash.config.logging.testclasses.Child3Config)method:defineChild3Valuevalue:Child3ConfigValueConfigimplementation:configNameScenario3(be.atbash.config.logging.testclasses.ParentConfig)method:defineParentValuevalue:ParentConfigValue");
    }

}
