package zombe.core.util;

public class KeyBind {

    public final int code;

    public final String name;

    public KeyBind(int code) {
        this.code = code;
        this.name = KeyHelper.getKeyName(code);
    }

    public KeyBind(String name) {
        this.name = name;
        this.code = KeyHelper.getKeyId(name);
    }

    public String toString() {
        return name;
    }

}
