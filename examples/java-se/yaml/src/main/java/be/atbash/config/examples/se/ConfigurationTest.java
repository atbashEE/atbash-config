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
        System.out.println("level.deeper.value2=" + config.getValue("level.deeper.value2", String.class));
        System.out.println("dateValue=" + config.getValue("dateValue", Date.class));
        System.out.println("test.only=" + config.getOptionalValue("test.only", String.class));
        System.out.println("nonExisting=" + config.getOptionalValue("nonExisting", String.class));
        //System.out.println("nonExisting="+config.getValue("nonExisting", String.class)); ;
    }
}
