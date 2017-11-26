package be.atbash.config.examples.se;

import be.atbash.config.spi.BaseConfigurationName;

/**
 *
 */

public class DemoBaseName implements BaseConfigurationName {
    @Override
    public String getBase() {
        return "demo";
    }

}
