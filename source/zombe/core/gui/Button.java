package zombe.core.gui;


import zombe.core.util.Color;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class Button extends Widget {

    public static final Color COLOR_ON = new Color(0x339900);
    public static final Color COLOR_OFF = new Color(0x990000);

    public Button(@Nullable String name) {
        this(name, null);
    }

    public Button(@Nullable String name, @Nullable Object value) {
        super(name, value);
    }

    @Nonnull
    @Override
    public Color defaultColor() {
        Object value = this.getValue();

        if (value instanceof Boolean) {
            return (((Boolean) value) ? COLOR_ON : COLOR_OFF);
        } else {
            return super.defaultColor();
        }
    }

    @Override
    public void draw() {
        this.setBorder((this.isActivated() || this.isClicked()) ? 1 : 0);
        super.draw();
    }
}
