package zombe.core.config;

import java.lang.*;

public class ConstraintBoolean extends TypeConstraint {

    public ConstraintBoolean() {}

    @Override
    public Boolean parseString(String value) {
        if (value == null) throw new NullPointerException();
        if (value.equals("yes") || value.equals("true") || value.equals("1")) return true;
        if (value.equals("no") || value.equals("false") || value.equals("0")) return false;
        throw new NumberFormatException();
    }

    @Override
    public String toString(Object o) {
        if (o == null) return null;
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? "yes" : "no";
        } else throw new IllegalArgumentException();
    }
}
