package be.atbash.config.examples.ee;

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
