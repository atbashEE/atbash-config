package be.atbash.config.test.converters;

import org.eclipse.microprofile.config.spi.Converter;

/**
 *
 */

public class ClassConverter implements Converter<Class> {
    public static final Converter<Class> INSTANCE = new ClassConverter();

    @Override
    public Class convert(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
