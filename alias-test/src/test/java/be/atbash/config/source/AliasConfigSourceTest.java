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
package be.atbash.config.source;

import be.atbash.util.resource.internal.ResourceWalker;
import com.google.common.collect.ImmutableList;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasConfigSourceTest {

    private TestLogger logger;

    @Before
    public void setup() {
        logger = TestLoggerFactory.getTestLogger(AliasConfigSource.class);
    }

    @After
    public void reset() {
        TestLoggerFactory.clear();
    }

    @Test
    public void testKeyReplaced() {
        // 'key' is the new key, and only value defined for 'oldKey'
        Config config = ConfigProvider.getConfig();
        String value = config.getValue("key", String.class);
        assertThat(value).isEqualTo("value");

        ImmutableList<LoggingEvent> loggingEvents = logger.getAllLoggingEvents();
        assertThat(loggingEvents).hasSize(1);
        assertThat(loggingEvents.asList().get(0).getMessage()).isEqualTo("Found a configuration value for deprecated key 'oldKey'. Please use the new key 'key'");
        assertThat(loggingEvents.asList().get(0).getLevel().name()).isEqualTo("INFO");
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
        String value = config.getOptionalValue("opt1", String.class);
        assertThat(value).isNull();
    }
}