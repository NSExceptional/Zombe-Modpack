package zombe.core.config;

import java.lang.*;

public class ConstraintText extends TypeConstraint {

    public ConstraintText() {}

    @Override
    public String parseString(String value) {
        return value;
    }

    @Override
    public String toString(Object o) {
        if (o == null) return null;
        if (o instanceof String) {
            return (String) o;
        } else throw new IllegalArgumentException();
    }
}
