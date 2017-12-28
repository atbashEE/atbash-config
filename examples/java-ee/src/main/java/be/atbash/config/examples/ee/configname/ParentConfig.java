package be.atbash.config.examples.ee.configname;

import be.atbash.config.logging.ConfigEntry;

/**
 *
 */

public class ParentConfig {

    @ConfigEntry
    public String defineParentValue() {
        return "Parent Config Value";
    }

}
