package zombe.core.util;


import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import zombe.core.ZWrapper;

import javax.annotation.Nonnull;
import java.nio.*;

public final class GuiHelper {

    public static final int ALIGN_LEFT = -1, ALIGN_CENTER = 0, ALIGN_RIGHT = 1;

    /**
     * Draws an uniform rect.
     *
     * Contrary to ZWrapper.drawRect(), it expects width and height args
     * instead of a pair [start;end[ of screen coordinates.
     * Note: color in ARGB format
     */
    public static void drawRect(int x, int y, int w, int h, int color) {
        ZWrapper.drawRect(x, y, x + w, y + h, color);
    }

    public static void drawRect(int x, int y, int w, int h, int color1, int color2) {
        ZWrapper.drawGradientRect(x, y, x + w, y + h, color1, color2);
    }

    public static void drawQuadBox(float sx, float sy, float sz, float ex, float ey, float ez) {
        GL11.glBegin(GL11.GL_QUADS);
        drawBox(sx, sy, sz, ex, ey, ez);
        GL11.glEnd();
    }

    public static void drawLineBox(float sx, float sy, float sz, float ex, float ey, float ez) {
        GL11.glBegin(GL11.GL_LINES);
        drawBox(sx, sy, sz, ex, ey, ez);
        GL11.glEnd();
    }

    private static void drawBox(float sx, float sy, float sz, float ex, float ey, float ez) {
        GL11.glVertex3f(sx, sy, sz);
        GL11.glVertex3f(sx, sy, ez);
        GL11.glVertex3f(sx, ey, ez);
        GL11.glVertex3f(sx, ey, sz);

        GL11.glVertex3f(ex, sy, sz);
        GL11.glVertex3f(ex, sy, ez);
        GL11.glVertex3f(ex, ey, ez);
        GL11.glVertex3f(ex, ey, sz);

        GL11.glVertex3f(sx, sy, sz);
        GL11.glVertex3f(sx, ey, sz);
        GL11.glVertex3f(ex, ey, sz);
        GL11.glVertex3f(ex, sy, sz);

        GL11.glVertex3f(sx, sy, ez);
        GL11.glVertex3f(sx, ey, ez);
        GL11.glVertex3f(ex, ey, ez);
        GL11.glVertex3f(ex, sy, ez);

        GL11.glVertex3f(sx, sy, sz);
        GL11.glVertex3f(ex, sy, sz);
        GL11.glVertex3f(ex, sy, ez);
        GL11.glVertex3f(sx, sy, ez);

        GL11.glVertex3f(sx, ey, sz);
        GL11.glVertex3f(ex, ey, sz);
        GL11.glVertex3f(ex, ey, ez);
        GL11.glVertex3f(sx, ey, ez);
    }

    /**
     * Shows text at given screen coordinates
     * Note: color in TRGB format
     */
    public static void showText(@Nonnull String str, int x, int y, int color) {
        ZWrapper.drawStringWithShadow(str, x, y, color);
    }

    public static void showTextAlign(@Nonnull String str, int x, int y, int w, int alignment, int color) {
        showText(str, x + ((w - showTextLength(str)) * (alignment + 1)) / 2, y, color);
    }

    public static void showTextCenter(@Nonnull String str, int x, int y, int w, int color) {
        showTextAlign(str, x, y, w, 0, color);
    }

    public static void showTextRight(@Nonnull String str, int x, int y, int w, int color) {
        showTextAlign(str, x, y, w, 1, color);
    }

    /** @return the length of a string in pixels */
    public static int showTextLength(@Nonnull String str) {
        return ZWrapper.getStringWidth(str);
    }

    @Nonnull
    public static String trimStringToWidth(@Nonnull String text, int width) {
        return (width >= 0) ? ZWrapper.trimStringToWidth(text, width) : StringHelper.reverse(ZWrapper.trimStringToWidth(StringHelper.reverse(text), -width));
    }

    /** Draws an item icon at given screen coordinates */
    private static void drawItem(@Nonnull ItemStack obj, int x, int y) {
        int meta = ZWrapper.getMeta(obj);
        if (meta == ZWrapper.ID_ANY) {
            ZWrapper.setMeta(obj, 0); // meta fix
        }
        ZWrapper.renderItemGUI(x, y, obj);
        ZWrapper.setMeta(obj, meta); // restore meta
    }

    /** Configures camera for GUI drawing */
    public static void setOrtho() {
        GL11.glOrtho(0.0D, ZWrapper.getScaledWidthD(), ZWrapper.getScaledHeightD(), 0.0D, 1000.0D, 3000.0D);
    }

    /** Configures camera for World drawing, with customizable near-plane clip */
    public static void setObliqueNearPlaneClip(float a, float b, float c, float d) {
        float matrix[] = new float[16];
        float x, y, z, w, dot;
        FloatBuffer buf = makeBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
        buf.get(matrix).rewind();
        x = (sgn(a) + matrix[8]) / matrix[0];
        y = (sgn(b) + matrix[9]) / matrix[5];
        z = -1.0F;
        w = (1.0F + matrix[10]) / matrix[14];
        dot = a * x + b * y + c * z + d * w;
        matrix[2] = a * (2f / dot);
        matrix[6] = b * (2f / dot);
        matrix[10] = c * (2f / dot) + 1.0F;
        matrix[14] = d * (2f / dot);
        buf.put(matrix).rewind();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadMatrix(buf);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private static float sgn(float f) {
        return f < 0f ? -1f : (f > 0f ? 1f : 0f);
    }

    private static FloatBuffer makeBuffer(int length) {
        return ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Nonnull
    private static FloatBuffer makeBuffer(@Nonnull float[] array) {
        return (FloatBuffer) ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).flip();
    }
}
