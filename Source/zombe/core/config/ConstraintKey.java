package zombe.core.config;

import java.lang.*;
import zombe.core.util.KeyHelper;
import zombe.core.util.KeyBind;

public class ConstraintKey extends TypeConstraint {

    public ConstraintKey() {}

    @Override
    public KeyBind parseString(String value) {
        if (value == null) return null;
        KeyBind key = new KeyBind(value);
        if (key.code == -1) throw new NumberFormatException();
        return key;
    }

    @Override
    public String toString(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) {
            int val = (Integer) o;
            return KeyHelper.getKeyName((Integer) o);
        } else if (o instanceof KeyBind) {
            KeyBind val = (KeyBind) o;
            return val.name;
        } else throw new IllegalArgumentException();
    }
}
