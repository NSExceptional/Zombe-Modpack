package zombe.core.config;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.*;

/** Represents a configuration option type and provides methods to query option values from files */
public abstract class OptionConstraint<T> {

    @Nonnull public abstract T defaultValue();
    @Nonnull public abstract T getMax();
    @Nonnull public T getMin() {
        return this.defaultValue();
    }

    /** @return the string parsed into a usable value */
    @Nonnull
    public abstract T parse(@Nonnull String string) throws BadValueException;

    /** @return the string parsed into a usable value, or null if it could not be parsed */
    @Nullable
    public T tryParse(@Nonnull String string) {
        try {
            return this.parse(string);
        } catch (BadValueException e) {
            e.printStackTrace();
            // TODO log
            return null;
        }
    }

    /** @return the string parsed into a usable value, or the constraint's default value */
    public T parsedOrDefault(@Nonnull String string) {
        T ret = this.tryParse(string);
        return ret == null ? this.defaultValue() : this.fix(ret);
    }

    /** @return whether the string falls within the constraint as is */
    public boolean canParse(@Nonnull String string) {
        return this.tryParse(string) != null;
    }

    /** @return the next-best or default value for the given value if necessary */
    @Nonnull
    public T fix(@Nonnull T value) {
        return value;
    }

    /** @return whether the value is valid as-is. */
    public boolean valueIsValid(@Nonnull T value) {
        return value.equals(this.fix(value));
    }

    @Nullable
    public abstract String toString(@Nonnull Object value);
}
