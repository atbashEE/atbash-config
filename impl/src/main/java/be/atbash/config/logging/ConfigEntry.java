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

import be.atbash.util.PublicAPI;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Methods annotated with ConfigEntry will be shown in the log file during startup of the container.
 */
@Target({METHOD})
@Retention(RUNTIME)
@PublicAPI
public @interface ConfigEntry {
    /**
     * Define the value for the log entry because the method can't or shouldn't be executed just for logging purposes.
     *
     * @return Array of String values which are used to print out in the log.
     */
    String[] value() default {};

    /**
     * Special  case of the value attribute where the method returns a class.
     *
     * @return Type to be logged
     */
    Class<?> classResult() default Void.class;

    /**
     * When true, the value of the ConfigEntry isn't logged (only marked if there is a value or not) except when the property -Datbash.config.log.all is set to true.
     *
     * @return should value be logged or only presence indicated.
     */
    boolean noLogging() default false;
}
