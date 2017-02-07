package zombe.core.config;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntegerConstraint extends IntervalConstraint<Integer> {

    public IntegerConstraint(int min, int max) {
        super(min, max);
    }

    @Nonnull
    @Override
    public Integer defaultValue() {
        return this.fix(0);
    }

    @Nonnull
    @Override
    public Integer parse(@Nonnull String string) throws BadValueException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            // TODO log
            throw new BadValueException();
        }
    }

    @Nullable
    @Override
    public String toString(@Nonnull Object value) {
        return null;
    }
}
