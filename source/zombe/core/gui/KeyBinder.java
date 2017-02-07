package zombe.core.gui;


import org.lwjgl.input.Keyboard;
import zombe.core.util.KeyBind;
import zombe.core.util.KeyHelper;

public class KeyBinder extends Widget {

    public KeyBinder(String name) {
        super(name, null);
    }

    @Override
    public void keyTyped(char c, int key) {
        if (!hasFocus()) {
            return;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            setFocused(false);
        } else if (key != Keyboard.KEY_NONE) {
            setValue(new KeyBind(key));
            setFocused(false);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (hasFocus()) {
            setValue(new KeyBind(KeyHelper.MOUSE | mouseButton));
            setFocused(false);
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void onActivation() {
        setFocused(this);
        setValue(new KeyBind(Keyboard.KEY_NONE));
    }

    @Override
    public void draw() {
        setBorder(hasFocus() ? 1 : 0);
        super.draw();
    }

    @Override
    public String getText() {
        if (hasFocus()) {
            return "_";
        }
        Object value = getValue();
        if (value instanceof Integer) {
            int val = (Integer) value;
            return KeyHelper.getKeyName(val);
        }
        if (value instanceof KeyBind) {
            KeyBind val = (KeyBind) value;
            return val.name;
        }
        return super.getText();
    }

}
