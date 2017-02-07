package zombe.core.gui;


import javax.annotation.Nonnull;

public class ScrollBar extends Slider {

    private float inner, outer;

    public ScrollBar(String name, @Nonnull Scale scale, @Nonnull Axis axis, float innerlength, float outerlength) {
        super(name, scale, 0, 0);
        setAxis(axis);
        if (scale == Scale.DISCRETE) {
            setValue((Integer) 0);
        } else {
            setValue((Float) 0f);
        }
        setLengths(innerlength, outerlength);
    }

    public ScrollBar(String name, @Nonnull Scale scale, @Nonnull Axis axis) {
        this(name, scale, axis, 0, 0);
    }

    public void setLengths(float inner, float outer) {
        this.inner = inner;
        this.outer = outer;
        if (inner <= outer) {
            setRange(0, 0);
            setFit(1f);
        } else {
            setRange(0, inner - outer);
            setFit(outer / inner);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            deactivate();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && !contains(mouseX, mouseY) && isActivated()) {
            setValueHovered(mouseX, mouseY);
        }
    }

    @Override
    public void drawValue() {
    }

}
