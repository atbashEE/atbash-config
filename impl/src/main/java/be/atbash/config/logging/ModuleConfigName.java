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
package be.atbash.config.logging;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Classes implementing ModuleConfig annotated with this annotation will be using the module config name within the logging.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface ModuleConfigName {
    /**
     * Define the value for the name used in the logging.
     *
     * @return module config name to be used in logging.
     */
    String value();

    /**
     * Defines if the className of the configuration class is still shown within the log.
     *
     * @return by default false, not showing the class name.
     */
    boolean className() default false;

}
