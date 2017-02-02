package zombe.core.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Represents a two-state option, such as on/off, true/false, etc. */
public class ToggleConstraint extends OptionConstraint<Boolean> {
    private static final Set<String> enableValues   = new HashSet<String>(Arrays.asList("yes", "true", "1"));
    private static final Set<String> disabledValues = new HashSet<String>(Arrays.asList("no", "false", "0"));

    public ToggleConstraint() {}

    public boolean read(@Nonnull String string) throws BadValueException {
        return this.parse(string);
    }

    public boolean readOrDefault(@Nonnull String string) {
        return this.parsedOrDefault(string);
    }

    @Nonnull
    @Override
    public Boolean defaultValue() {
        return false;
    }

    @Nonnull
    @Override
    public Boolean getMax() {
        return true;
    }

    @Nonnull
    @Override
    public Boolean parse(@Nonnull String string) throws BadValueException {
        string = string.toLowerCase();
        if (disabledValues.contains(string)) {
            return false;
        }
        if (enableValues.contains(string)) {
            return true;
        }

        try {
            // Any integer value is acceptable
            return Integer.parseInt(string) != 0;
        } catch (NumberFormatException e) {
            // TODO log
            throw new BadValueException();
        }
    }

    @Nonnull
    @Override
    public String toString(@Nonnull Object value) {
        Boolean fixed = this.parseObject(value);
        if (fixed != null) {
            return this.stringValue(fixed);
        }

        throw new IllegalArgumentException();
    }

    /** @return the object parsed into a boolean, or null if no assumptions could be made */
    @Nullable
    private Boolean parseObject(@Nonnull Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Integer) {
            return (Integer) value != 0;
        }
        if (value instanceof String) {
            return this.tryParse((String) value);
        }

        return null;
    }


    /** Converts boolean to String */
    @Nonnull
    public final String stringValue(boolean b) {
        return b ? "yes" : "no";
    }
}
