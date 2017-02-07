package zombe.core.config;


import javax.annotation.Nonnull;

public class FloatConstraint extends IntervalConstraint<Double> {

    public FloatConstraint(float min, float max) {
        super((double) min, (double) max);
    }

    public FloatConstraint(double min, double max) {
        super(min, max);
    }

    @Nonnull
    @Override
    public Double defaultValue() {
        return this.fix(0.0);
    }

    @Nonnull
    @Override
    public Double parse(@Nonnull String string) throws BadValueException {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            // TODO log
            throw new BadValueException();
        }
    }

    @Override
    public String toString(@Nonnull Object value) {
        return null;
    }
}
