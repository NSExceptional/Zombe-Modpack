package zombe.core.gui;


import zombe.core.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Label extends Widget {

    private static final Color DEFAULT_COLOR = new Color(0xff000000);

    public Label(@Nullable String value) {
        super(null, value);
        this.setAlignment(-1);
    }

    public Label(String value, int alignment) {
        super(null, value);
        this.setAlignment(alignment);
    }

    public Label(String name, String value) {
        super(name, value);
        this.setAlignment(-1);
    }

    @Nonnull
    @Override
    public Color defaultColor() {
        return DEFAULT_COLOR;
    }

    @Override
    protected void onActivation() {
        this.setActivated(false);
    }

}
