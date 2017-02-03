package zombe.core.gui;


import zombe.core.ZWrapper;
import zombe.core.util.Color;
import zombe.core.util.KeyBind;
import zombe.core.util.GuiHelper;

public abstract class Widget {

    public static final Color COLOR_DEFAULT = new Color(0x444444);
    public static final Color BORDER_DEFAULT = new Color(0x666666);
    public static final Color BORDER_FOCUSED = new Color(0x00ccff);
    public static final Color BORDER_ACTIVATED = new Color(0x00ccff);
    public static final Color BORDER_HOVERED = new Color(0x999999);

    private static Widget focusedWidget = null;
    private static Widget clickedWidget = null;
    private static Widget hoveredWidget = null;

    public static Widget getFocused() {
        return focusedWidget;
    }

    public static Widget getClicked() {
        return clickedWidget;
    }

    public static Widget getHovered() {
        return hoveredWidget;
    }

    public static void setFocused(Widget widget) {
        focusedWidget = widget;
    }

    public static void clearClicked() {
        clickedWidget = null;
    }

    public static void clearHovered() {
        hoveredWidget = null;
    }

    private String name;

    private Object value = null;

    private String defaultText = null;

    private Color color = null;

    private int posX, posY, width, height;

    private boolean activated, hovered, clicked;

    private int border = 0;

    private int alignment = 0;

    public Widget(String name, Object value) {
        this.name = name;
        this.value = null;
        this.activated = false;
        this.hovered = false;
        this.clicked = false;
        this.setValue(value);
    }

    public Widget(String name) {
        this(name, null);
    }

    public final String getName() {
        return this.name;
    }

    public final Object getValue() {
        return this.value;
    }

    public String getDefaultText() {
        return this.defaultText;
    }

    public final Color getColor() {
        return this.color;
    }

    public final int getBorder() {
        return this.border;
    }

    public final int getAlignment() {
        return this.alignment;
    }

    public Color defaultColor() {
        return COLOR_DEFAULT;
    }

    public final int getPosX() {
        return this.posX;
    }

    public final int getPosY() {
        return this.posY;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public boolean isActivated() {
        return this.activated;
    }

    public boolean hasFocus() {
        return getFocused() == this;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public void setFocused(boolean focus) {
        if (this.hasFocus() == focus) {
            return;
        }
        if (focus) {
            setFocused(this);
        } else {
            setFocused(null);
        }
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setDefaultText(String text) {
        this.defaultText = text;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setPosition(int x, int y, int w, int h) {
        this.posX = x;
        this.posY = y;
        this.width = w;
        this.height = h;
    }

    public boolean contains(int x, int y) {
        return this.posX <= x && x < this.posX + this.width && this.posY <= y && y < this.posY + this.height;
    }

    public void keyTyped(char c, int key) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.contains(mouseX, mouseY)) {
            this.activate();
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
    }

    protected boolean checkHovered() {
        int mouseX = Keys.getMouseX();
        int mouseY = Keys.getMouseY();
        return this.contains(mouseX, mouseY);
    }

    protected boolean checkClicked() {
        return this.isHovered() && Keys.wasMousePressedThisFrame(0)
                || Keys.isMouseDownThisFrame(0) && this.clicked && this.isActivated();
    }

    public void checkState() {
        if (getFocused() != null && !this.hasFocus()) {
            this.hovered = false;
            this.clicked = false;
            return;
        }
        this.hovered = this.checkHovered();
        if (this.hovered) {
            hoveredWidget = this;
        }
        this.clicked = this.checkClicked();
        if (this.clicked) {
            clickedWidget = this;
        }
    }

    public void activate() {
        if (this.isActivated()) {
            return;
        }
        this.setActivated(true);
        this.onActivation();
    }

    public void deactivate() {
        if (!this.isActivated()) {
            return;
        }
        this.setActivated(false);
        this.onDeactivation();
    }

    protected void onActivation() {
    }

    protected void onDeactivation() {
    }

    public Color getActualColor() {
        Color col = this.color == null ? this.defaultColor() : this.color;
        if (col == null || !this.isHovered()) {
            return col;
        }
        return new Color(col.rf + 0.125f, col.gf + 0.125f, col.bf + 0.125f, col.af);
    }

    public Color getBorderColor() {
        return this.hasFocus() ? BORDER_FOCUSED
                : this.isActivated() ? BORDER_ACTIVATED
                : this.isHovered() ? BORDER_HOVERED : BORDER_DEFAULT;
    }

    public void draw() {
        this.drawColor(this.getActualColor(), this.getBorderColor());
        this.drawValue();
    }

    public void drawColor(Color color, Color bord) {
        if (color == null) {
            return;
        }
        if (this.border > 0 && bord != null) {
            GuiHelper.drawRect(this.posX, this.posY, this.width, this.height, bord.rgba);
            GuiHelper.drawRect(this.posX + this.border, this.posY + this.border, this.width - this.border * 2, this.height - +this.border * 2, color.rgba);
        } else {
            GuiHelper.drawRect(this.posX, this.posY, this.width, this.height, color.rgba);
        }
    }

    public String getText() {
        if (this.value instanceof String) {
            return (String) this.value;
        }
        if (this.value instanceof Boolean) {
            return ((Boolean) this.value) ? "yes" : "no";
        }
        if (this.value instanceof Float) {
            return String.valueOf((Float) this.value);
        }
        if (this.value instanceof Integer) {
            return String.valueOf((Integer) this.value);
        }
        if (this.value instanceof KeyBind) {
            return ((KeyBind) this.value).name;
        }
        return null;
    }

    public String getActualText() {
        String text = this.getText();
        if (text == null) {
            return this.getDefaultText();
        }
        return text;
    }

    public void drawValue() {
        this.drawText(this.getActualText());
    }

    public void drawText(String text) {
        if (text == null) {
            return;
        }
        int space = this.width - this.border * 2;
        text = GuiHelper.trimStringToWidth(text, this.getAlignment() > 0 ? -space : space);
        GuiHelper.showTextAlign(text, this.posX + this.border, this.posY + (this.height - 8) / 2, space, this.getAlignment(), 0xffffff);
    }
}
