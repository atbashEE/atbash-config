package be.atbash.config.examples.ee.configname;

import be.atbash.config.logging.ConfigEntry;
import be.atbash.config.logging.ModuleConfig;

import javax.enterprise.context.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
//@ModuleConfigName(value = "Module Config with classes", className = true)
public class ChildConfig extends ParentConfig implements ModuleConfig {

    @ConfigEntry
    public String defineChildValue() {
        return "Child Config Value";
    }

}
