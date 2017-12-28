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

import be.atbash.config.logging.testclasses.Child1Config
import be.atbash.config.logging.testclasses.Child2Config
import be.atbash.config.logging.testclasses.Child3Config
import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import spock.lang.Specification

/**
 *
 */

class ModuleConfigNameTest extends Specification {


    @Collaborator
    DynamicConfigValueHelper valueHelper = new DynamicConfigValueHelper()

    @Subject
    StartupLogging logging

    def moduleConfigName_Scenario1() {
        // No @ModuleConfigName
        when:
        String info = logging.getConfigInfo(new Child1Config())
        String data = info.replaceAll("\\s", "")

        then:
        data == "Configimplementation:be.atbash.config.logging.testclasses.Child1Configmethod:defineChild1Valuevalue:Child1ConfigValueConfigimplementation:be.atbash.config.logging.testclasses.ParentConfigmethod:defineParentValuevalue:ParentConfigValue"
    }

    def moduleConfigName_Scenario2() {
        // @ModuleConfigName, no className
        when:
        String info = logging.getConfigInfo(new Child2Config())
        String data = info.replaceAll("\\s", "")

        then:
        data == "configNameScenario2:method:defineChild2Valuevalue:Child2ConfigValuemethod:defineParentValuevalue:ParentConfigValue"
    }

    def moduleConfigName_Scenario3() {
        // @ModuleConfigName, with className
        when:
        String info = logging.getConfigInfo(new Child3Config())
        String data = info.replaceAll("\\s", "")

        then:
        data == "Configimplementation:configNameScenario3(be.atbash.config.logging.testclasses.Child3Config)method:defineChild3Valuevalue:Child3ConfigValueConfigimplementation:configNameScenario3(be.atbash.config.logging.testclasses.ParentConfig)method:defineParentValuevalue:ParentConfigValue"
    }

}
