package zombe.core.gui;

import zombe.core.ZWrapper;
import zombe.core.util.Color;
import zombe.core.util.GuiHelper;

public class Label extends Widget {

    private static final Color DEFAULT_COLOR = new Color(0xff000000);

    public Label(String value) {
        super(null, value);
        setAlignment(-1);
    }

    public Label(String value, int alignment) {
        super(null, value);
        setAlignment(alignment);
    }
    
    public Label(String name, String value) {
        super(name, value);
        setAlignment(-1);
    }

    @Override
    protected void onActivation()  {
        setActivated(false);
    }

    @Override
    public Color defaultColor() {
        return DEFAULT_COLOR;
    }

}
