package zombe.core.config;


import javax.annotation.Nonnull;
import java.lang.*;

public abstract class IntervalConstraint<T extends Comparable<T>> extends OptionConstraint<T> {
    private final T min;
    private final T max;

    public IntervalConstraint(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public T normalize(T o) {
        return (this.min.compareTo(o) <= 0)
             ? (this.max.compareTo(o) < 0 ? this.max : o)
             : this.min;
    }

    @Nonnull
    @Override
    public T getMin() {
        return this.min;
    }
    @Nonnull
    @Override
    public T getMax() {
        return this.max;
    }

    @Nonnull
    @Override
    public T fix(@Nonnull T value) {
        return this.normalize(value);
    }
}
