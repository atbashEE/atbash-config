package be.atbash.config.examples.se;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Date;

/**
 *
 */

public class ConfigurationTest {

    public static void main(String[] args) {
        Config config = ConfigProvider.getConfig();

        System.out.println("value1=" + config.getValue("value1", String.class));
        System.out.println("value2=" + config.getValue("value2", Integer.class));
        System.out.println("value3=" + config.getValue("value3", Boolean.class));
        System.out.println("dateValue=" + config.getValue("dateValue", Date.class));
        System.out.println("nonExisting=" + config.getOptionalValue("nonExisting", String.class));
        System.out.println("testOnly=" + config.getOptionalValue("testOnly", String.class));
        //System.out.println("nonExisting="+config.getValue("nonExisting", String.class));
    }
}
