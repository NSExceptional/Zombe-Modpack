package zombe.core.gui;


import zombe.core.config.IntegerConstraint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScrollBar extends Slider<Integer> {

    private double inner, outer;

    public ScrollBar(@Nullable String name, @Nonnull Axis axis, double innerlength, double outerlength) {
        super(name, Scale.DISCRETE, new IntegerConstraint(0, 0));
        this.setAxis(axis);
        this.setValue(0);
        this.setLengths(innerlength, outerlength);
    }

    public ScrollBar(@Nullable String name, @Nonnull Axis axis) {
        this(name, axis, 0, 0);
    }

    public void setLengths(double inner, double outer) {
        this.inner = inner;
        this.outer = outer;
        if (inner <= outer) {
            this.setRange(0, 0);
            this.setFit(1f);
        } else {
            this.setRange(0, inner - outer);
            this.setFit(outer / inner);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            this.deactivate();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && !this.contains(mouseX, mouseY) && this.isActivated()) {
            this.setValueHovered(mouseX, mouseY);
        }
    }

    @Override
    public void drawValue() {
    }
}
