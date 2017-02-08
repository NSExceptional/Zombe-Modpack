package zombe.core.util;


import java.lang.*;

import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public final class KeyHelper {

    /** Is this a made up magic number? */
    public static final int MOUSE = 0x10000;
    public static final int MOUSE_SIZE = 32;
    public static final int KEY_NONE = Keyboard.KEY_NONE;

    @Nonnull
    public static String getKeyName(int key) {
        if (key == Keyboard.KEY_NONE) {
            return "NONE";
        }
        if ((key & MOUSE) != 0) {
            return "MOUSE" + (key ^ MOUSE);
        }

        String res = Keyboard.getKeyName(key);
        return res != null ? res : "" + key;
    }

    public static int getKeyID(@Nonnull String name) {
        if (name.isEmpty() || name.equals("NONE")) {
            return Keyboard.KEY_NONE;
        }

        try {
            name = name.toUpperCase();
            Integer button = null;

            // Remove prefix from "MOUSE3" like strings
            if (name.startsWith("MOUSE")) {
                button = Integer.parseInt(name.substring(5));
            }
            if (name.startsWith("BUTTON")) {
                button = Integer.parseInt(name.substring(6));
            }

            if (button != null) {
                if (button >= 0 && button < 256) {
                    return button | MOUSE;
                }

                return -1;
            }

            return Keyboard.getKeyIndex(name);
        } catch (Exception e) {
            return -1;
        }
    }
}
