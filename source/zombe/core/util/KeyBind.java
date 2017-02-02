package zombe.core.util;

public class KeyBind {
    public static KeyBind NONE = new KeyBind(KeyHelper.KEY_NONE);

    public final int code;
    public final String name;

    public KeyBind(int code) {
        this.code = code;
        this.name = KeyHelper.getKeyName(code);
    }

    public KeyBind(String name) {
        this.name = name;
        this.code = KeyHelper.getKeyID(name);
    }

    public String toString() {
        return name;
    }
}
