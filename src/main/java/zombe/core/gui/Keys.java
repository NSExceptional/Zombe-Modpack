package zombe.core.gui;

import zombe.core.ZWrapper;
import zombe.core.util.KeyHelper;
import java.lang.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class Keys {

    private static Keys keysTick = new Keys();
    private static Keys keysFrame = new Keys();
    private static int scaledW, scaledH, mouseX, mouseY;

    public static void newTick() {
        keysTick.flip();
    }
    public static void newFrame() {
        keysFrame.flip();
        
        scaledW = ZWrapper.getScaledWidth();
        scaledH = ZWrapper.getScaledHeight();

        mouseX = Mouse.getX() * scaledW / ZWrapper.getScreenWidth();
        mouseY = scaledH - Mouse.getY() * scaledH / ZWrapper.getScreenHeight() -1;
    }

    public static int getScaledW() { return scaledW; }
    public static int getScaledH() { return scaledH; }
    public static int getMouseX() { return mouseX; }
    public static int getMouseY() { return mouseY; }

    public static int getKeyDownThisTick() { return keysTick.getKeyDown(); }
    public static int getKeyDownThisFrame() { return keysFrame.getKeyDown(); }

    public static int getKeyPressedThisTick() { return keysTick.getKeyPressed(); }
    public static int getKeyPressedThisFrame() { return keysFrame.getKeyPressed(); }

    public static boolean isKeyDownThisTick(int key) { return keysTick.isKeyDown(key); }
    public static boolean isKeyDownThisFrame(int key) { return keysFrame.isKeyDown(key); }

    public static boolean isMouseDownThisTick(int button) { return isKeyDownThisTick(button | KeyHelper.MOUSE); }
    public static boolean isMouseDownThisFrame(int button) { return isKeyDownThisFrame(button | KeyHelper.MOUSE); }

    public static boolean wasKeyPressedThisTick(int key) { return keysTick.wasKeyPressed(key); }
    public static boolean wasKeyPressedThisFrame(int key) { return keysFrame.wasKeyPressed(key); }

    public static boolean wasMousePressedThisTick(int button) { return wasKeyPressedThisTick(button | KeyHelper.MOUSE); }
    public static boolean wasMousePressedThisFrame(int button) { return wasKeyPressedThisFrame(button | KeyHelper.MOUSE); }

    private boolean keysPrev[] = new boolean[Keyboard.KEYBOARD_SIZE];
    private boolean keysDown[] = new boolean[Keyboard.KEYBOARD_SIZE];

    private boolean mousePrev[] = new boolean[KeyHelper.MOUSE_SIZE];
    private boolean mouseDown[] = new boolean[KeyHelper.MOUSE_SIZE];


    private Keys() {}

    private void flip() {
        boolean[] tmp;

        tmp = keysPrev;
        keysPrev = keysDown;
        keysDown = tmp;

        for (int i = 1; i < keysDown.length; ++i) {
            keysDown[i] = Keyboard.isKeyDown(i);
        }

        tmp = mousePrev;
        mousePrev = mouseDown;
        mouseDown = tmp;

        for (int i = 0; i < mouseDown.length && i < Mouse.getButtonCount(); ++i) {
            mouseDown[i] = Mouse.isButtonDown(i);
        }
    }

    private int getKeyDown() {
        for (int i = 1; i < keysDown.length; ++i)
            if (keysDown[i]) return i;
        for (int i = 0; i < mouseDown.length && i < Mouse.getButtonCount(); ++i)
            if (mouseDown[i]) return i | KeyHelper.MOUSE;
        return Keyboard.KEY_NONE;
    }

    private int getKeyPressed() {
        for (int i = 1; i < keysDown.length; ++i)
            if (keysDown[i] && !keysPrev[i]) return i;
        for (int i = 0; i < mouseDown.length && i < Mouse.getButtonCount(); ++i)
            if (mouseDown[i] && !mousePrev[i]) return i | KeyHelper.MOUSE;
        return Keyboard.KEY_NONE;
    }

    private boolean isKeyDown(int key) {
        if ((key & KeyHelper.MOUSE) != 0) {
            int button = key ^ KeyHelper.MOUSE;
            if (button < mouseDown.length) 
                return mouseDown[button];
        } else if (key < keysDown.length)
            return keysDown[key];
        return false;
    }

    private boolean wasKeyPressed(int key) {
        if ((key & KeyHelper.MOUSE) != 0) {
            int button = key ^ KeyHelper.MOUSE;
            if (button < mouseDown.length) 
                return mouseDown[button] && !mousePrev[button];
        } else if (key < keysDown.length)
            return keysDown[key] && !keysPrev[key];
        return false;
    }

}
