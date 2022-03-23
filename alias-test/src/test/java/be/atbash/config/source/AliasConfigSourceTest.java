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
package be.atbash.config.source;

import be.atbash.runtime.logging.testing.LoggingEvent;
import be.atbash.runtime.logging.testing.TestLogMessages;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasConfigSourceTest {

    @BeforeEach
    public void setup() {
        TestLogMessages.init();
    }

    @AfterEach
    public void reset() {
        TestLogMessages.reset();
    }

    @Test
    public void testKeyReplaced() {
        // 'key' is the new key, and only value defined for 'oldKey'
        Config config = ConfigProvider.getConfig();
        String value = config.getValue("key", String.class);
        assertThat(value).isEqualTo("value");

        List<LoggingEvent> loggingEvents = TestLogMessages.getLoggingEvents();
        // depending if we run this test standalone or as part of this class, the number of messages
        // can be different. We just need the last message.
        int logMessages = loggingEvents.size();
        assertThat(loggingEvents.get(logMessages - 1).getMessage()).isEqualTo("Found a configuration value for deprecated key 'oldKey'. Please use the new key 'key'");
        assertThat(loggingEvents.get(logMessages - 1).getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    public void testPrecedence1() {
        // 'key1' is the new key, 'key2' the old.
        // There is a mapping from old to new, but new value is found
        Config config = ConfigProvider.getConfig();
        String value = config.getValue("key1", String.class);
        assertThat(value).isEqualTo("value1");
    }

    @Test
    public void testPrecedence2() {
        // 'key1' is the new key, 'key2' the old.
        // There is a mapping from old to new, but old value still can be looked up
        Config config = ConfigProvider.getConfig();
        String value = config.getValue("key2", String.class);
        assertThat(value).isEqualTo("value2");
    }

    @Test
    public void testOptional() {
        Config config = ConfigProvider.getConfig();
        Optional<String> value = config.getOptionalValue("opt1", String.class);
        assertThat(value.isPresent()).isFalse();
    }
}