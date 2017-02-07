package zombe.core.gui;


import zombe.core.config.*;
import zombe.core.util.Color;
import zombe.core.util.GuiHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Pls only use T as Double or Integer, thx */
public class Slider<T extends Number & Comparable<T>> extends Widget {

    public static final Color COLOR_SLIDER_CURSOR = new Color(0xffffff);
    public static final Color COLOR_SLIDER_CAPTION = new Color(0xffffcc);

    @Nonnull private Scale scale;
    @Nonnull private Axis axis;
    @Nonnull private IntervalConstraint<T> range;
    private double fit = 0f;
    private int offset = 0;

    public enum Scale {
        DISCRETE, LINEAR, LOG
    }

    public enum Axis {
        HORIZONTAL, VERTICAL
    }

    public Slider(@Nullable String name, @Nonnull Scale scale, @Nonnull IntervalConstraint<T> constraint) {
        super(name, null);
        this.scale = scale;
        this.axis = Axis.HORIZONTAL;
        this.range = constraint;
        this.setBorder(1);
    }

    protected static double clampRatio(double ratio) {
        return (ratio < 0) ? 0 : (ratio > 1) ? 1 : ratio;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public T getValue() {
        T value = (T) super.getValue();
        if (value == null) {
            throw new NullPointerException();
        }

        return (T) super.getValue();
    }

    public void setValue(@Nonnull T value) {
        super.setValue(value);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && this.contains(mouseX, mouseY)) {
            this.setValueHovered(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            this.setFocused(false);
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && this.contains(mouseX, mouseY) && this.isActivated()) {
            this.setValueHovered(mouseX, mouseY);
        }
    }

    @Override
    protected boolean checkHovered() {
        return this.getCursorLength() < this.getAxisLength() && super.checkHovered();
    }

    @Override
    protected boolean checkClicked() {
        return this.getCursorLength() < this.getAxisLength() && super.checkClicked();
    }

    @Override
    protected void onActivation() {
        setFocused(this);
        int length = this.getCursorLength();
        this.offset = length / 2;

        if (this.hasValue()) {
            double value = this.getValue().doubleValue();

            int cursor = this.getCursorFromRatio(this.getRatioFromValue(value));
            int click = this.byAxis(Keys.getMouseX(), Keys.getMouseY()) - this.getPosOnAxis();
            if (cursor <= click && click < cursor + length) {
                this.offset = click - cursor;
            }
        }
    }

    @Override
    protected void onDeactivation() {
        this.offset = this.getCursorLength() / 2;
    }

    @Nonnull
    @Override
    public Color getBorderColor() {
        return (this.fit <= 0 && !this.isActivated()) ? COLOR_SLIDER_CURSOR : super.getBorderColor();
    }

    @Override
    public void drawColor(@Nullable Color fill, @Nullable Color border) {
        if (fill == null) {
            return;
        }

        int posx = this.byAxis(this.getPosX(), this.getPosX() + (this.getWidth() - 1) / 2);
        int posy = this.byAxis(this.getPosY() + (this.getHeight() - 1) / 2, this.getPosY());
        int width = this.byAxis(this.getWidth(), 2);
        int height = this.byAxis(2, this.getHeight());
        GuiHelper.drawRect(posx, posy, width, height, fill.rgba);

        if (this.hasValue()) {
            double value = this.getValue().doubleValue();

            // TODO make sure this will never be possible
            if (Double.isNaN(value)) {
                value = this.range.min.doubleValue();
            }

            double ratio = this.getRatioFromValue(value);
            int cursor = this.getCursorFromRatio(ratio);
            int length = this.getCursorLength();
            int bor = border == null ? 0 : this.getBorder();

            // TODO sure what's going on here
            if (this.axis == Axis.HORIZONTAL) {
                if (bor > 0) {
                    GuiHelper.drawRect(this.getPosX() + cursor, this.getPosY(), length, this.getHeight(), border.rgba);
                }
                if (length > bor * 2) {
                    GuiHelper.drawRect(this.getPosX() + cursor + bor, this.getPosY() + bor, length - bor * 2, this.getHeight() - bor * 2, fill.rgba);
                }
            } else {
                if (bor > 0) {
                    GuiHelper.drawRect(this.getPosX(), this.getPosY() + cursor, this.getWidth(), length, border.rgba);
                }
                if (length > bor * 2) {
                    GuiHelper.drawRect(this.getPosX() + bor, this.getPosY() + cursor + bor, this.getWidth() - bor * 2, length - bor * 2, fill.rgba);
                }
            }
        }
    }

    @Override
    public void drawValue() {
        if (this.isHovered() && (Widget.getFocused() == null || this.hasFocus())) {
            String caption = String.format((this.scale == Scale.DISCRETE) ? "%.0f" : "%.2f", this.getValueHovered(Keys.getMouseX(), Keys.getMouseY()));

            if (this.hasValue()) {
                double value = this.getValue().doubleValue();
                if (!this.isActivated()) {
                    caption += String.format((this.scale == Scale.DISCRETE) ? " (%.0f)" : " (%.2f)", value);
                }
            } else {
                super.drawValue();
            }

            GuiHelper.drawRect(Keys.getMouseX() + 1, Keys.getMouseY() - 9, GuiHelper.showTextLength(caption), 8, 0x80000000);
            GuiHelper.showText(caption, Keys.getMouseX() + 1, Keys.getMouseY() - 9, COLOR_SLIDER_CAPTION.rgba);
        }
    }

    @Nullable
    public T tryGetValue() {
        //noinspection unchecked
        return (T) super.getValue();
    }

    public boolean hasValue() {
        return super.getValue() != null;
    }

    @Nullable
    public T clampValue(@Nullable T value) {
        if (value != null) {
            return this.range.normalize(value);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void setRange(double min, double max) {
        if (this.range instanceof FloatConstraint) {
            this.range = (IntervalConstraint<T>) new FloatConstraint(min, max);
        } else if (this.range instanceof IntegerConstraint) {
            this.range = (IntervalConstraint<T>) new IntegerConstraint((int) min, (int) max);
        }

        if (this.hasValue()) {
            this.setValue(this.clampValue(this.getValue()));
        }
    }

    public double getMin() {
        return this.range.min.doubleValue();
    }

    public double getMax() {
        return this.range.max.doubleValue();
    }

    public double getFit() {
        return this.fit;
    }

    public void setFit(double fit) {
        this.fit = fit;
    }

    @Nonnull
    public Axis getAxis() {
        return this.axis;
    }

    public void setAxis(@Nonnull Axis axis) {
        this.axis = axis;
    }

    @Nonnull
    public Scale getScale() {
        return this.scale;
    }

    public void setScale(@Nonnull Scale scale) {
        this.scale = scale;
    }

    protected int getAxisLength() {
        return this.axis == Axis.HORIZONTAL ? this.getWidth() : this.getHeight();
    }

    protected int getCursorLength() {
        return (int) Math.round(1 + this.fit * (this.getAxisLength() - 1));
    }

    protected int getCursorFromRatio(double ratio) {
        return (int) Math.round((this.getAxisLength() - this.getCursorLength()) * ratio);
    }

    protected double getRatioFromCursor(int cursor) {
        double ratio = cursor / (double) (this.getAxisLength() - this.getCursorLength());
        return Double.isNaN(ratio) ? 0f : ratio;
    }

    protected double getRatioFromValue(double value) {
        double min = this.range.min.doubleValue();
        double max = this.range.max.doubleValue();

        double ratio = (this.scale == Scale.LOG) ? Math.log(value / min) / Math.log(max / min) : (value - min) / (max - min);
        return Double.isNaN(ratio) ? 0f : ratio;
    }

    protected double getValueFromRatio(double ratio) {
        double min = this.range.min.doubleValue();
        double max = this.range.max.doubleValue();

        double value = (this.scale == Scale.LOG) ? min * Math.pow(max / min, ratio) : min + ratio * (max - min);
        return Double.isNaN(value) ? min : value;
    }

    protected int getCursorHovered(int mouseX, int mouseY) {
        int mouse = this.byAxis(mouseX, mouseY) - this.getPosOnAxis();

        return this.isActivated() ? mouse - this.offset : mouse - this.getCursorLength() / 2;
    }

    protected double getValueHovered(int mouseX, int mouseY) {
        int cursor = this.getCursorHovered(mouseX, mouseY);
        return this.getValueFromRatio(clampRatio(this.getRatioFromCursor(cursor)));
    }

    protected void setValueHovered(int mouseX, int mouseY) {
        double val = this.getValueHovered(mouseX, mouseY);

        if (this.scale == Scale.DISCRETE) {
            this.setValue(Math.round(val));
        } else {
            this.setValue(val);
        }
    }

    /** @return this.getPosX() if horizontal, else this.getPosY() */
    protected int getPosOnAxis() {
        return this.axis == Axis.HORIZONTAL ? this.getPosX() : this.getPosY();
    }

    /** @return x if horizontal, else y */
    protected int byAxis(int x, int y) {
        return this.axis == Axis.HORIZONTAL ? x : y;
    }
}
