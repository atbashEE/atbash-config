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

/**
 *
 */

public class Pet {

    private String value;

    public String getValue() {
        return value;
    }

    public static Pet valueOf(String value) {
        Pet result = new Pet();
        result.value = value;
        return result;
    }

    @Override
    public String toString() {
        return "Pet{" + "value='" + value + '\'' + '}';
    }
}
