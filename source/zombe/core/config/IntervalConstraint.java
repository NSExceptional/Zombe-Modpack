package zombe.core.config;


import javax.annotation.Nonnull;

public abstract class IntervalConstraint<T extends Number & Comparable<T>> extends OptionConstraint<T> {
    public final T min;
    public final T max;

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
    public T fix(@Nonnull T value) {
        return this.normalize(value);
    }
}
