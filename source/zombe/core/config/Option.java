package zombe.core.config;


import javax.annotation.Nonnull;

/** Represents a configurable setting for the mod. Does not contain the setting itself. */
public class Option<T> {
    public static final ToggleConstraint  BOOL = new ToggleConstraint();
    public static final TextConstraint    TEXT = new TextConstraint();
    public static final KeyBindConstraint KEY  = new KeyBindConstraint();

    @Nonnull public final String category;
    @Nonnull public final String description;
    @Nonnull public final OptionConstraint<T> constraint;
    @Nonnull public final T defaultValue;

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
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public String defaultString() {
        return this.constraint.toString(this.defaultValue);
    }
}
