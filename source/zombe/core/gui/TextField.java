package zombe.core.gui;


import org.lwjgl.input.Keyboard;
import zombe.core.util.GuiHelper;

public class TextField extends Widget {

    private static final String CURSOR_TEXT = "\u00A76|\u00A7f";
    private int cursor;

    public TextField(String name) {
        super(name, null);
    }

    @Override
    public void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RETURN) {
            setFocused(false);
        } else {
            String value = (String) getValue();
            if (key == Keyboard.KEY_BACK) {
                if (cursor == 0) {
                    return;
                }
                --cursor;
                setValue(value.substring(0, cursor) + value.substring(cursor + 1, value.length()));
            } else if (key == Keyboard.KEY_DELETE) {
                if (cursor == value.length()) {
                    return;
                }
                setValue(value.substring(0, cursor) + value.substring(cursor + 1, value.length()));
            } else if (key == Keyboard.KEY_HOME) {
                cursor = 0;
            } else if (key == Keyboard.KEY_END) {
                cursor = value.length();
            } else if (key == Keyboard.KEY_LEFT) {
                if (cursor > 0) {
                    --cursor;
                }
            } else if (key == Keyboard.KEY_RIGHT) {
                if (cursor < value.length()) {
                    ++cursor;
                }
            } else if (c != Keyboard.CHAR_NONE) {
                setValue(value.substring(0, cursor) + c + value.substring(cursor, value.length()));
                ++cursor;
            }
            // TODO: selection, ^X, ^C, ^V
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (hasFocus()) {
            // TODO: cursor placement
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        // TODO: selection
    }

    @Override
    protected void onActivation() {
        setFocused(this);
        if (getValue() == null) {
            setValue("");
        }
        cursor = ((String) getValue()).length();
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void onDeactivation() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void draw() {
        setBorder(hasFocus() ? 1 : 0);
        //setAlignment(hasFocus() ? -1 : 0);
        super.draw();
    }

    @Override
    public String getText() {
        if (hasFocus()) {
            String cursorPart = CURSOR_TEXT;
            String value = (String) getValue();
            int halfWidth = (getWidth() - GuiHelper.showTextLength(cursorPart) - getBorder() * 2) / 2;
            String leftPart = value.substring(0, cursor);
            String rightPart = value.substring(cursor, value.length());
            int leftWidth = GuiHelper.showTextLength(leftPart);
            int rightWidth = GuiHelper.showTextLength(rightPart);
            int leftMax = (rightWidth > halfWidth) ? halfWidth : 2 * halfWidth - rightWidth;
            int rightMax = (leftWidth > halfWidth) ? halfWidth : 2 * halfWidth - leftWidth;
            leftPart = GuiHelper.trimStringToWidth(leftPart, -leftMax);
            rightPart = GuiHelper.trimStringToWidth(rightPart, rightMax);
            return leftPart + cursorPart + rightPart;
        } else {
            return super.getText();
        }
    }

}
