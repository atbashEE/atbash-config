package be.atbash.config.examples.ee;

import be.atbash.config.logging.ConfigEntry;
import be.atbash.config.logging.ModuleConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 */
@ApplicationScoped
public class ApplicationConfiguration implements ModuleConfig {

    @Inject
    private Config config;

    @Inject
    @ConfigProperty(name = "value1")
    private String value1;

    @Inject
    @ConfigProperty(name = "testOnly", defaultValue = "Not within Test")
    private String testOnly;

    @ConfigEntry
    public Integer getValue2() {
        return config.getValue("value2", Integer.class);
    }

    @ConfigEntry
    public String getValue1() {
        return value1;
    }

    @ConfigEntry
    public String getTestOnly() {
        return testOnly;
    }
}
