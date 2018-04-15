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

import be.atbash.config.ConfigOptionalValue;
import be.atbash.config.exception.ConfigurationException;
import be.atbash.ee.security.octopus.jwt.decoder.JWTDecoder;
import be.atbash.util.StringUtils;
import be.atbash.util.exception.AtbashUnexpectedException;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 */

public class ConfigServerConfigSource implements ConfigSource {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigServerConfigSource.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w*");

    private Map<String, String> properties;

    private void init() {
        if (properties == null) {
            properties = new HashMap<>();
            String configServerURL = null;
            String application = null;
            try {
                configServerURL = ConfigProvider.getConfig().getValue("config.server.url", String.class);
                application = ConfigProvider.getConfig().getValue("config.server.app", String.class);
            } catch (NoSuchElementException e) {
                throw new ConfigurationException(e.getMessage());
            }
            String stage = ConfigOptionalValue.getValue("config.server.stage", String.class);

            checkValue(application, true, "config.server.app");
            checkValue(stage, false, "config.server.stage");

            WebTarget target = ClientBuilder.newClient().target(defineConfigServerEndpoint(configServerURL, application, stage));
            Response response = target.request().buildGet().invoke();

            if (response.getStatus() == 200) {
                String config = response.readEntity(String.class);
                JWTDecoder decoder = new JWTDecoder();
                properties.putAll(decoder.decode(config, HashMap.class));
            }

            if (response.getStatus() == 204) {
                throw new ConfigurationException(String.format("Application '%s' not known by the Atbash Config Server", application));
            }

            if (response.getStatus() > 204) {
                throw new AtbashUnexpectedException(response.readEntity(String.class));
            }
        }
    }

    private void checkValue(String value, boolean required, String parameterName) {
        if (StringUtils.isEmpty(value) && required) {
            throw new ConfigurationException(String.format("parameter '%s' is required", parameterName));
        }

        if (StringUtils.isEmpty(value)) {
            return;
        }

        if (!WORD_PATTERN.matcher(value).matches()) {
            throw new ConfigurationException(String.format("parameter '%s' can contain only alphabet characters and digits", parameterName));
        }
    }

    private String defineConfigServerEndpoint(String configServerURL, String application, String stage) {
        StringBuilder result = new StringBuilder();
        if (configServerURL.endsWith("/")) {
            result.append(configServerURL).append("config/");
        } else {
            result.append(configServerURL).append("/config/");
        }

        result.append(application);
        if (StringUtils.hasText(stage)) {
            result.append("?stage=").append(stage);
        }
        LOG.info(String.format("Reading configuration values from %s", result.toString()));
        return result.toString();
    }

    @Override
    public Map<String, String> getProperties() {
        init();
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        init();

        return properties.keySet();
    }

    @Override
    public String getValue(String s) {
        init();
        return properties.get(s);
    }

    @Override
    public String getName() {
        return "Octopus Config Server ";
    }

    @Override
    public int getOrdinal() {
        return 350;
    }
}
