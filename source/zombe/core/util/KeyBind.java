package zombe.core.util;


import javax.annotation.Nonnull;

public class KeyBind {
    @Nonnull public static KeyBind NONE = new KeyBind(KeyHelper.KEY_NONE);

    public final int code;
    @Nonnull public final String name;

    public KeyBind(int code) {
        this.code = code;
        this.name = KeyHelper.getKeyName(code);
    }

    public KeyBind(@Nonnull String name) {
        this.name = name;
        this.code = KeyHelper.getKeyID(name);
    }

    @Nonnull
    public String toString() {
        return name;
    }
}
