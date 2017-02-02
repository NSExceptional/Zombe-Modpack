package zombe.core.config;

import javax.annotation.Nonnull;
import java.lang.*;

/** Represents a configurable setting for the mod. Does not contain the setting itself. */
public class Option<T> {
    public static final OptionConstraint BOOL = new ToggleConstraint();
    public static final OptionConstraint TEXT = new TextConstraint();
    public static final OptionConstraint KEY  = new KeyBindConstraint();

    public final String category;
    public final String description;
    public final OptionConstraint<T> constraint;
    public final T defaultValue;

    @Nonnull
    public Option(@Nonnull String category,
                  @Nonnull String description,
                  @Nonnull OptionConstraint<T> constraint,
                  @Nonnull T defaultValue) {
        if (!constraint.valueIsValid(defaultValue)) {
            throw new IllegalArgumentException();
        }

        this.category     = category;
        this.description  = description;
        this.constraint   = constraint;
        this.defaultValue = defaultValue;
    }

    /** @return the default value as converted to a string by the constraint */
    public String defaultString() {
        return this.constraint.toString(this.defaultValue);
    }
}
