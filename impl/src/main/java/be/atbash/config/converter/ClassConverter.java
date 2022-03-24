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
package be.atbash.config.converter;

import be.atbash.util.reflection.ClassUtils;
import org.eclipse.microprofile.config.spi.Converter;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Vetoed;

@Priority(100)
@Vetoed
public class ClassConverter implements Converter<Class<?>> {

    @Override
    public Class<?> convert(String value) {
        if (value == null) {
            return null;
        }
        return ClassUtils.forName(value);
    }
}
