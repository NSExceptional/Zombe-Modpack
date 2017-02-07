package zombe.core.gui;


import zombe.core.util.Color;

import javax.annotation.Nonnull;

public class Button extends Widget {

    public static final Color COLOR_ON = new Color(0x339900);
    public static final Color COLOR_OFF = new Color(0x990000);

    public Button(String name) {
        this(name, null);
    }

    public Button(String name, Object value) {
        super(name, value);
    }

    @Nonnull
    @Override
    public Color defaultColor() {
        Object value = getValue();
        return (value instanceof Boolean) ? (((Boolean) value) ? COLOR_ON : COLOR_OFF) : super.defaultColor();
    }

    @Override
    public void draw() {
        setBorder((isActivated() || isClicked()) ? 1 : 0);
        super.draw();
    }

}
