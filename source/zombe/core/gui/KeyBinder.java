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
        if (!this.hasFocus()) {
            return;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            this.setFocused(false);
        } else if (key != Keyboard.KEY_NONE) {
            this.setValue(new KeyBind(key));
            this.setFocused(false);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.hasFocus()) {
            this.setValue(new KeyBind(KeyHelper.MOUSE | mouseButton));
            this.setFocused(false);
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void onActivation() {
        setFocused(this);
        this.setValue(new KeyBind(Keyboard.KEY_NONE));
    }

    @Override
    public void draw() {
        this.setBorder(this.hasFocus() ? 1 : 0);
        super.draw();
    }

    @Override
    public String getText() {
        if (this.hasFocus()) {
            return "_";
        }

        Object value = this.getValue();
        if (value instanceof Integer) {
            return KeyHelper.getKeyName((Integer) value);
        }

        if (value instanceof KeyBind) {
            KeyBind val = (KeyBind) value;
            return val.name;
        }

        return super.getText();
    }
}
