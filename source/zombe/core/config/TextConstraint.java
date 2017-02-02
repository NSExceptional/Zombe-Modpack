package zombe.core.config;

import javax.annotation.Nonnull;
import java.lang.*;

public class TextConstraint extends OptionConstraint<String> {

    public TextConstraint() {}

    @Nonnull
    @Override
    public String parse(@Nonnull String value) throws BadValueException {
        return value;
    }

    @Nonnull
    @Override
    public String defaultValue() {
        return "";
    }

    @Nonnull
    @Override
    public String getMax() {
        return "";
    }

    @Override
    public String toString(@Nonnull Object value) {
        if (value instanceof String) {
            return (String) value;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
