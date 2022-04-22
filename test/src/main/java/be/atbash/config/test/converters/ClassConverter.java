package be.atbash.config.test.converters;

import be.atbash.util.reflection.ClassUtils;
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

        return ClassUtils.forName(value);

    }

}
