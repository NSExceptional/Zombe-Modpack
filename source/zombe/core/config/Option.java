package zombe.core.config;


import java.util.*;
import java.lang.*;

public class Option {

    public static final TypeConstraint BOOL = new ConstraintBoolean();
    public static final TypeConstraint TEXT = new ConstraintText();
    public static final TypeConstraint KEY = new ConstraintKey();

    private final String category;
    private final String description;
    private final TypeConstraint constraint;
    private final Object defaultValue;

    public Option(String category, String description, TypeConstraint constraint, Object defaultValue) {
        if (category == null || description == null || constraint == null || defaultValue == null) throw new NullPointerException();
        if (!constraint.good(defaultValue)) throw new IllegalArgumentException();
        this.category = category;
        this.description = description;
        this.constraint = constraint;
        this.defaultValue = defaultValue;
    }

    public String getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public TypeConstraint getConstraint() {
        return this.constraint;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }
    
    public String getDefaultString() {
        return getConstraint().toString(getDefaultValue());
    }
}
