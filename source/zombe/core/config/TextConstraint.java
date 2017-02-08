package zombe.core.config;


import javax.annotation.Nonnull;

public class TextConstraint extends OptionConstraint<String> {

    public TextConstraint() {
    }

    @Nonnull
    @Override
    public String defaultValue() {
        return "";
    }

    @Nonnull
    @Override
    public String parse(@Nonnull String value) throws BadValueException {
        return value;
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
