package zombe.core.config;


import java.util.*;
import java.lang.*;

public abstract class TypeConstraint {

    public abstract Object parseString(String value);

    public Object fromString(String value) {
        return fix(parseString(value));
    }

    public String toString(Object o) {
        if (o == null) return null;
        return o.toString();
    }

    public boolean typeMatches(String value) {
        try {
            Object o = parseString(value);
            return o != null;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean constraintMatches(String value) {
        try {
            Object o = parseString(value);
            return o != null && good(o);
        } catch (Exception e) {
            return false;
        }
    }

    public Object getMin() {
        return null;
    }
    public Object getMax() {
        return null;
    }
    public Object fix(Object o) {
        return o;
    }
    public boolean good(Object o) {
        return o.equals(fix(o));
    }

}
