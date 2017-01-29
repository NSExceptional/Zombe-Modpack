package zombe.core.gui;

import zombe.core.ZWrapper;
import zombe.core.util.Color;
import zombe.core.util.KeyBind;
import zombe.core.util.GuiHelper;

public abstract class Widget {

    public static final Color COLOR_DEFAULT    = new Color(0x444444);
    public static final Color BORDER_DEFAULT   = new Color(0x666666);
    public static final Color BORDER_FOCUSED   = new Color(0x00ccff);
    public static final Color BORDER_ACTIVATED = new Color(0x00ccff);
    public static final Color BORDER_HOVERED   = new Color(0x999999);

    private static Widget focusedWidget = null;
    private static Widget clickedWidget = null;
    private static Widget hoveredWidget = null;

    public static Widget getFocused() { return focusedWidget; }

    public static Widget getClicked() { return clickedWidget; }

    public static Widget getHovered() { return hoveredWidget; }

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
        setValue(value);
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

    public final int getPosX() { return this.posX; }

    public final int getPosY() { return this.posY; }

    public final int getWidth() { return this.width; }

    public final int getHeight() { return this.height; }

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
        if (this.hasFocus() == focus) return;
        if (focus) setFocused(this);
        else setFocused(null);
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
        return posX <= x && x < posX + width && posY <= y && y < posY + height;
    }

    public void keyTyped(char c, int key) {}

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && contains(mouseX,mouseY)) activate();
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {}

    protected boolean checkHovered() {
        int mouseX = Keys.getMouseX();
        int mouseY = Keys.getMouseY();
        return contains(mouseX, mouseY);
    }

    protected boolean checkClicked() {
        return isHovered() && Keys.wasMousePressedThisFrame(0)
            || Keys.isMouseDownThisFrame(0) && this.clicked && isActivated();
    }

    public void checkState() {
        if (getFocused() != null && !hasFocus()) {
            hovered = false;
            clicked = false;
            return;
        }
        this.hovered = checkHovered();
        if (this.hovered) hoveredWidget = this;
        this.clicked = checkClicked();
        if (this.clicked) clickedWidget = this;
    }

    public void activate() {
        if (isActivated()) return;
        setActivated(true);
        onActivation();
    }

    public void deactivate() {
        if (!isActivated()) return;
        setActivated(false);
        onDeactivation();
    }

    protected void onActivation() {}

    protected void onDeactivation() {}

    public Color getActualColor() {
        Color col = color == null ? defaultColor() : color;
        if (col == null || !isHovered()) return col;
        return new Color(col.rf +0.125f, col.gf +0.125f, col.bf +0.125f, col.af);
    }

    public Color getBorderColor() {
        return hasFocus() ? BORDER_FOCUSED
             : isActivated() ? BORDER_ACTIVATED
             : isHovered() ? BORDER_HOVERED : BORDER_DEFAULT;
    }

    public void draw() {
        drawColor(getActualColor(), getBorderColor());
        drawValue();
    }

    public void drawColor(Color color, Color bord) {
        if (color == null) return;
        if (border > 0 && bord != null) {
            GuiHelper.drawRect(posX, posY, width, height, bord.rgba);
            GuiHelper.drawRect(posX+border, posY+border, width-border*2, height-+border*2, color.rgba);
        } else {
            GuiHelper.drawRect(posX, posY, width, height, color.rgba);
        }
    }

    public String getText() {
        if (value instanceof String)  return (String) value;
        if (value instanceof Boolean) return ((Boolean) value) ? "yes" : "no";
        if (value instanceof Float)   return String.valueOf((Float) value);
        if (value instanceof Integer) return String.valueOf((Integer) value);
        if (value instanceof KeyBind) return ((KeyBind) value).name;
        return null;
    }

    public String getActualText() {
        String text = getText();
        if (text == null) return getDefaultText();
        return text;
    }

    public void drawValue() {
        drawText(getActualText());
    }

    public void drawText(String text) {
        if (text == null) return;
        int space = width-border*2;
        text = GuiHelper.trimStringToWidth(text, getAlignment() > 0 ? -space : space);
        GuiHelper.showTextAlign(text, posX+border, posY+(height-8)/2, space, getAlignment(), 0xffffff);
    }
}
