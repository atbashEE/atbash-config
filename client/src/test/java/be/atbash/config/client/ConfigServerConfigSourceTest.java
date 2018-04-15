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
package be.atbash.config.client;

import be.atbash.config.exception.ConfigurationException;
import be.atbash.config.test.TestConfig;
import be.atbash.util.exception.AtbashUnexpectedException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Tried with Spock but very complex and no clear error messages (Exception was masked by something else)
 */

public class ConfigServerConfigSourceTest {

    private WireMockServer wireMockServer;
    private WireMock wireMock;

    @Before
    public void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        wireMock = new WireMock("localhost", wireMockServer.port());

        TestConfig.registerDefaultConverters();
    }

    @After
    public void teardown() {
        TestConfig.resetConfig();
        wireMockServer.stop();

    }

    @Test
    public void getValue() {
        // Classic success scenario
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());
        String appValue = "testApp";
        TestConfig.addConfigValue("config.server.app", appValue);

        wireMock.register(WireMock.get(WireMock.urlEqualTo("/config/" + appValue))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("{\"data\":\"value\"}")
                ));

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        String data = configSource.getValue("data");

        assertThat(data).isEqualTo("value");
        wireMock.verifyThat(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/config/" + appValue)));
    }

    @Test
    public void getValue_unknownApp() {
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());
        String appValue = "testApp";
        TestConfig.addConfigValue("config.server.app", appValue);

        wireMock.register(WireMock.get(WireMock.urlEqualTo("/config/" + appValue))
                .willReturn(WireMock.aResponse()
                        .withStatus(204)
                ));

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();

        try {
            configSource.getValue("data");
            fail("ConfigurationException expected for unknown application");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("Application 'testApp' not known by the Atbash Config Server");
        }

        wireMock.verifyThat(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/config/" + appValue)));
    }

    @Test
    public void getValue_unknownException() {
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());
        String appValue = "testApp";
        TestConfig.addConfigValue("config.server.app", appValue);

        wireMock.register(WireMock.get(WireMock.urlEqualTo("/config/" + appValue))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withBody("The Server exception")
                ));

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();

        try {
            configSource.getValue("data");
            fail("AtbashUnexpectedException expected for Server error");
        } catch (AtbashUnexpectedException e) {
            assertThat(e.getMessage()).isEqualTo("The Server exception");
        }

        wireMock.verifyThat(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/config/" + appValue)));
    }

    @Test
    public void getValue_MissingServerUrl() {

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        try {
            configSource.getValue("data");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("Key config.server.url does not exists");
        }

    }

    @Test
    public void getValue_MissingApplication() {
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        try {
            configSource.getValue("data");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("Key config.server.app does not exists");
        }

    }

    @Test
    public void getValue_InvalidURL() {
        TestConfig.addConfigValue("config.server.url", "blablabla");
        TestConfig.addConfigValue("config.server.app", "test");

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        try {
            configSource.getValue("data");
        } catch (ProcessingException e) {
            assertThat(e.getMessage()).isEqualTo("URI is not absolute");
        }
    }

    @Test
    public void getValue_emptyApplication() {
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());
        TestConfig.addConfigValue("config.server.app", "");

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        try {
            configSource.getValue("data");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("parameter 'config.server.app' is required");
        }

    }

    @Test
    public void getValue_specialCharactersApplication() {
        TestConfig.addConfigValue("config.server.url", "http://localhost:" + wireMockServer.port());
        TestConfig.addConfigValue("config.server.app", "test&");

        ConfigServerConfigSource configSource = new ConfigServerConfigSource();
        try {
            configSource.getValue("data");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("parameter 'config.server.app' can contain only alphabet characters and digits");
        }

    }

}