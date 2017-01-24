package zombe.core.gui;

import zombe.core.util.GuiHelper;
import zombe.core.util.Color;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.Math;
import java.lang.IllegalArgumentException;

public class Slider extends Widget {

    public static final Color COLOR_SLIDER_CURSOR  = new Color(0xffffff);
    public static final Color COLOR_SLIDER_CAPTION = new Color(0xffffcc);

    public enum Scale {
        DISCRETE, LINEAR, LOG
    }

    public enum Axis {
        HORIZONTAL, VERTICAL
    }

    private Scale scale;
    private float min, max, fit = 0f;
    private Axis axis;
    private int offset = 0;

    public Slider(String name, Scale scale, float min, float max) {
        super(name, null);
        this.scale = scale;
        this.min = min;
        this.max = max;
        this.axis = Axis.HORIZONTAL;
        setBorder(1);
    }

    @Override
    protected void onActivation() {
        setFocused(this);
        int length = getCursorLength();
        offset = length / 2;
        Object value = getValue();
        if (value instanceof Float || value instanceof Integer) {
            float val = (value instanceof Float)
                ? (Float)value : (Integer) value;
            int cursor = getCursorFromRatio(getRatioFromValue(val));
            int click = (axis == Axis.HORIZONTAL)
                      ? Keys.getMouseX()-getPosX()
                      : Keys.getMouseY()-getPosY();
            if (cursor <= click && click < cursor+length) {
                offset = click-cursor;
            }
        }
    }

    @Override
    protected boolean checkHovered() {
        if (getCursorLength() >= getAxisLength()) return false;
        return super.checkHovered();
    }

    @Override
    protected boolean checkClicked() {
        if (getCursorLength() >= getAxisLength()) return false;
        return super.checkClicked();
    }

    @Override
    protected void onDeactivation() {
        offset = getCursorLength() / 2;
    }

    @Override
    public void setValue(Object value) {
        if (scale == Scale.DISCRETE && value instanceof Float
         || scale != Scale.DISCRETE && value instanceof Integer) {
            throw new IllegalArgumentException();
        }
        super.setValue(value);
    }

    public Object clampValue(Object value) {
        if (value instanceof Float || value instanceof Integer) {
            float val = (value instanceof Float)
                ? (Float)value : (Integer) value;
            val = (val < min) ? min : (val > max) ? max : val;
            if (value instanceof Float)
                return (Float)val;
            else
                return (Integer)Math.round(val);
        }
        return value;
    }

    protected static float clampRatio(float ratio) {
        return (ratio < 0) ? 0 : (ratio > 1) ? 1 : ratio;
    }

    public void setRange(float min, float max) {
        this.min = min;
        this.max = max;
        setValue(clampValue(getValue()));
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    public float getFit() {
        return this.fit;
    }

    public void setFit(float fit) {
        this.fit = fit;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public Scale getScale() {
        return this.scale;
    }

    protected int getAxisLength() {
        return axis == Axis.HORIZONTAL ? getWidth() : getHeight();
    }

    protected int getCursorLength() {
        return Math.round(1 + fit * (getAxisLength()-1));
    }

    protected int getCursorFromRatio(float ratio) {
        return Math.round((getAxisLength()-getCursorLength()) * ratio);
    }

    protected float getRatioFromCursor(int cursor) {
        float ratio = cursor / (float)(getAxisLength()-getCursorLength());
        return Float.isNaN(ratio) ? 0f : ratio;
    }

    protected float getRatioFromValue(float value) {
        float ratio = (scale == Scale.LOG)
                    ? (float) (Math.log(value/min) / Math.log(max/min))
                    : (value - min) / (max - min);
        return Float.isNaN(ratio) ? 0f : ratio;
    }

    protected float getValueFromRatio(float ratio) {
        float value = (scale == Scale.LOG)
                    ? min * (float)Math.pow(max/min, ratio)
                    : min + ratio * (max - min);
        return Float.isNaN(value) ? min : value;
    }

    protected int getCursorHovered(int mouseX, int mouseY) {
        int mouse = (axis == Axis.HORIZONTAL)
                  ? mouseX-getPosX() : mouseY-getPosY();
        return isActivated() ? mouse-offset : mouse-getCursorLength()/2;
    }

    protected float getValueHovered(int mouseX, int mouseY) {
        int cursor = getCursorHovered(mouseX, mouseY);
        return getValueFromRatio(clampRatio(getRatioFromCursor(cursor)));
    }

    protected void setValueHovered(int mouseX, int mouseY) {
        float val = getValueHovered(mouseX,mouseY);
        if (scale == Scale.DISCRETE)
            setValue((Integer)Math.round(val));
        else
            setValue((Float)val);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && contains(mouseX,mouseY)) {
            setValueHovered(mouseX,mouseY);
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
        super.mouseClickMove(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && contains(mouseX,mouseY) && isActivated()) {
            setValueHovered(mouseX,mouseY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) setFocused(false);
    }

    @Override
    public Color getBorderColor() {
        return (fit <= 0 && !isActivated()) ? COLOR_SLIDER_CURSOR : super.getBorderColor();
    }

    @Override
    public void drawColor(Color color, Color border) {
        if (color == null) return;
        if (axis == Axis.HORIZONTAL) {
            GuiHelper.drawRect(getPosX(), getPosY()+(getHeight()-1)/2, 
                               getWidth(), 2, color.rgba);
        } else {
            GuiHelper.drawRect(getPosX()+(getWidth()-1)/2, getPosY(), 
                               2, getHeight(), color.rgba);
        }

        Object value = getValue();
        if (value instanceof Float || value instanceof Integer) {
            float val = (value instanceof Float)
                ? (Float)value : (Integer)value;
            if (Float.isNaN(val)) val = this.min;
            float ratio = getRatioFromValue(val);
            int cursor = getCursorFromRatio(ratio);
            int length = getCursorLength();
            int bor = (border == null) ? 0 : getBorder();
            if (axis == Axis.HORIZONTAL) {
                if (bor > 0)        GuiHelper.drawRect(getPosX()+cursor, 
                    getPosY(), length, getHeight(), border.rgba);
                if (length > bor*2) GuiHelper.drawRect(getPosX()+cursor+bor,
                    getPosY()+bor, length-bor*2, getHeight()-bor*2, color.rgba);
            } else {
                if (bor > 0)        GuiHelper.drawRect(getPosX(), 
                    getPosY()+cursor, getWidth(), length, border.rgba);
                if (length > bor*2) GuiHelper.drawRect(getPosX()+bor, 
                    getPosY()+cursor+bor, getWidth()-bor*2, length-bor*2, color.rgba);
            }
        }
    }

    @Override
    public void drawValue() {
        Object value = getValue();
        String caption = null;
        if (isHovered() && (Widget.getFocused() == null || hasFocus())) {
            caption = String.format((scale == Scale.DISCRETE) ? "%.0f" : "%.2f", getValueHovered(Keys.getMouseX(), Keys.getMouseY()));
        }
        if (value instanceof Float || value instanceof Integer) {
            float val = (value instanceof Float)
                ? (Float)value : (Integer)value;
            if (caption != null && !isActivated()) {
                caption += String.format((scale == Scale.DISCRETE) ? " (%.0f)" : " (%.2f)", val);
            }
        } else super.drawValue();
        if (caption != null) {
            GuiHelper.drawRect(Keys.getMouseX()+1, Keys.getMouseY()-9, GuiHelper.showTextLength(caption), 8, 0x80000000);
            GuiHelper.showText(caption, Keys.getMouseX()+1, Keys.getMouseY()-9, COLOR_SLIDER_CAPTION.rgba);
        }
    }

}
