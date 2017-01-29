package zombe.core.config;


import java.util.*;
import java.lang.*;

public abstract class ConstraintInterval<T extends Comparable<T>> extends TypeConstraint {

    private final T min;
    private final T max;

    public ConstraintInterval(T min, T max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public abstract T parseString(String value);

    public T clamp(T o) {
        return (this.min.compareTo(o) <= 0)
             ? (this.max.compareTo(o) < 0 ? this.max : o) 
             : this.min;
    }

    @Override
    public Object getMin() {
        return this.min;
    }
    @Override
    public Object getMax() {
        return this.max;
    }
    @Override
    public Object fix(Object o) {
        return clamp((T) o);
    }
}
