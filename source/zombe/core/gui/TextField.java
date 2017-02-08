package zombe.core.gui;


import zombe.core.util.GuiHelper;
import org.lwjgl.input.Keyboard;

public class TextField extends Widget {

    private static final String CURSOR_TEXT = "\u00A76|\u00A7f";
    private int cursor;

    public TextField(String name) {
        super(name, null);
    }

    @Override
    public void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RETURN) {
            this.setFocused(false);
        } else {
            String value = (String) this.getValue();

            switch (key) {
                case Keyboard.KEY_BACK:
                    if (this.cursor == 0) {
                        return;
                    }
                    this.cursor--;
                    this.setValue(value.substring(0, this.cursor) + value.substring(this.cursor + 1, value.length()));
                    break;
                case Keyboard.KEY_DELETE:
                    if (this.cursor == value.length()) {
                        return;
                    }
                    this.setValue(value.substring(0, this.cursor) + value.substring(this.cursor + 1, value.length()));
                    break;
                case Keyboard.KEY_HOME:
                    this.cursor = 0;
                    break;
                case Keyboard.KEY_END:
                    this.cursor = value.length();
                    break;
                case Keyboard.KEY_LEFT:
                    if (this.cursor > 0) {
                        this.cursor--;
                    }
                    break;
                case Keyboard.KEY_RIGHT:
                    if (this.cursor < value.length()) {
                        this.cursor++;
                    }
                    break;
                case Keyboard.CHAR_NONE:
                    this.setValue(value.substring(0, this.cursor) + c + value.substring(this.cursor, value.length()));
                    this.cursor++;
                    break;

            }
            // TODO: selection, ^X, ^C, ^V
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.hasFocus()) {
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
        if (this.getValue() == null) {
            this.setValue("");
        }

        this.cursor = ((String) this.getValue()).length();
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void onDeactivation() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void draw() {
        this.setBorder(this.hasFocus() ? 1 : 0);
        //setAlignment(hasFocus() ? -1 : 0);
        super.draw();
    }

    @Override
    public String getText() {
        if (this.hasFocus()) {
            String cursorPart = CURSOR_TEXT;
            String value = (String) this.getValue();
            int halfWidth = (this.getWidth() - GuiHelper.showTextLength(cursorPart) - this.getBorder() * 2) / 2;
            String leftPart = value.substring(0, this.cursor);
            String rightPart = value.substring(this.cursor, value.length());
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
