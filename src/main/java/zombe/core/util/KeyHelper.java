package zombe.core.util;

import java.lang.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class KeyHelper {

    public static final int MOUSE = 0x10000;
    public static final int MOUSE_SIZE = 32;

    public static int getKeyNone() {
        return Keyboard.KEY_NONE;
    }

    public static String getKeyName(int key) {
        if (key == Keyboard.KEY_NONE) 
            return "NONE";
        if ((key & MOUSE) != 0)
            return "MOUSE"+(key ^ MOUSE);
        String res = Keyboard.getKeyName(key);
        return res != null ? res : ""+key;
    }

    public static int getKeyId(String name) {
        if (name.equals("") || name.equals("NONE")) return Keyboard.KEY_NONE;
        name = name.toUpperCase();
        String param = null;
        if (name.startsWith("MOUSE")) {
            param = name.substring(5);
        }
        if (name.startsWith("BUTTON")) {
            param = name.substring(6);
        }
        if (param != null) {
            try {
                int button = Integer.parseInt(param);
                if (button >= 0 && button < 256) return button | MOUSE;
            } catch (Exception e) {
            }
            return -1;
        }
        int key = Keyboard.getKeyIndex(name.toUpperCase());
        return key;
    }
}
