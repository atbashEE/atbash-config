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
package be.atbash.config.converter;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.Converter;

import javax.enterprise.inject.Vetoed;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
@Vetoed
public class AtbashDateConverter implements Converter<Date> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd";

    private boolean initialized;
    private String datePattern;

    @Override
    public Date convert(String value) {
        doInitialization();
        if (value != null) {
            try {
                if (value.contains(",")) {
                    String[] parts = value.split(",", 2);
                    return new SimpleDateFormat(parts[1]).parse(parts[0]);
                } else {

                    return new SimpleDateFormat(datePattern).parse(value);
                }
            } catch (ParseException pe) {
                throw new IllegalArgumentException(pe);
            }
        }
        return null;
    }

    private void doInitialization() {
        // We can't call this from constructor since we are creating the config at that point and thus ca't access it yet :)
        if (initialized) {
            return;
        }

        initialized = true;

        Config config = ConfigProvider.getConfig();
        datePattern = config.getOptionalValue("atbash.date.pattern", String.class);
        if (datePattern == null) {
            datePattern = DEFAULT_PATTERN;
        }

    }

}
