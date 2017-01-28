package zombe.core.config;

import java.lang.*;

public class ConstraintFloat extends ConstraintInterval<Float> {

    public ConstraintFloat(Float min, Float max) {
        super(min, max);
    }

    @Override
    public Float parseString(String value) {
        return Float.parseFloat(value);
    }
}
