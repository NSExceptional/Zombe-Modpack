package zombe.core.util;


import javax.annotation.Nonnull;

public class KeyBind {
    @Nonnull public static KeyBind NONE = new KeyBind(KeyHelper.KEY_NONE);

    @Nonnull public final String name;
    public final int code;

    public KeyBind(int code) {
        this.name = KeyHelper.getKeyName(code);
        this.code = code;
    }

    public KeyBind(@Nonnull String name) {
        this.name = name;
        this.code = KeyHelper.getKeyID(name);
    }

    @Nonnull
    public String toString() {
        return this.name;
    }
}
