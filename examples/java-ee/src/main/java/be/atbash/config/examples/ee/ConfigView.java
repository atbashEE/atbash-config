package be.atbash.config.examples.ee;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */
@RequestScoped
@Named
public class ConfigView {

    @Inject
    private Config config;

    @Inject
    @ConfigProperty(name = "value1")
    private String value1;

    @Inject
    @ConfigProperty(name = "testOnly", defaultValue = "Not within Test")
    private String testOnly;

    public Integer getValue2() {
        return config.getValue("value2", Integer.class);
    }

    public String getValue1() {
        return value1;
    }

    public String getTestOnly() {
        return testOnly;
    }
}
