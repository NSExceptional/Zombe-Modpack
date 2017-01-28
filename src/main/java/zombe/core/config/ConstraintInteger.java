package zombe.core.config;


import java.util.*;
import java.lang.*;

public class ConstraintInteger extends ConstraintInterval<Integer> {

    public ConstraintInteger(Integer min, Integer max) {
        super(min, max);
    }

    @Override
    public Integer parseString(String value) {
        return Integer.parseInt(value);
    }
}
