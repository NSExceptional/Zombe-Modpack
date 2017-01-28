package zombe.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collection;

/**
    Public handle for the modpack's hooks.

    Hooked Minecraft files shall not call ZModpack's methods directly:
    instead, they shall call ZHandle wrapper methods.
*/
public class ZHandle {

    /* GENERIC GETTERS & EVENTS */

    /**
        Handles an event and/or property request
        @param name The name of the event or property
        @param arg (Optional) Argument for the event or default value for the property
        @returns the value requested, if any, or null. (default is arg)
    */
    public static Object handle(String name, Object arg) {
        Object ret = arg;
        ZMod mod = ZMod.getHandler(name);
        if (mod != null && mod.isActive())
        try {
            ret = mod.handle(name, arg);
        } catch (Exception e) {
            ZMod.err("handle("+name+") error:", e);
        }
        Collection<ZMod> col = ZMod.getListeners(name);
        if (col != null)
        for (ZMod zmod : col)
        if (zmod.isActive())
        try {
            zmod.handle(name, arg);
        } catch (Exception e) {
            ZMod.err("handle("+name+") error:", e);
        }
        return ret;
    }

    public static Object handle(String name) {
        return handle(name, null);
    }

    public static boolean handle(String name, boolean arg) {
        Object obj = handle(name, (Boolean) arg);
        return (obj instanceof Boolean) ? (Boolean) obj : arg;
    }

    public static int handle(String name, int arg) {
        Object obj = handle(name, (Integer) arg);
        return (obj instanceof Integer) ? (Integer) obj : arg;
    }

    public static long handle(String name, long arg) {
        Object obj = handle(name, (Long) arg);
        return (obj instanceof Long) ? (Long) obj : arg;
    }

    public static float handle(String name, float arg) {
        Object obj = handle(name, (Float) arg);
        return (obj instanceof Float) ? (Float) obj : arg;
    }

    public static double handle(String name, double arg) {
        Object obj = handle(name, (Double) arg);
        return (obj instanceof Double) ? (Double) obj : arg;
    }

    public static boolean handle(String name, Object arg, boolean def) {
        Object obj = handle(name, arg);
        return (obj instanceof Boolean) ? (Boolean) obj : def;
    }

    /* EVENT HANDLERS */

    /* Minecraft */
    public static void onMinecraftInit(Minecraft mc) {
        ZModpack.initialize(mc);
    }

    // note: used to be pingUpdateHandle()
    public static void onMinecraftTick() {
        ZModpack.clientTick(ZWrapper.getPlayer());
    }

    /* ZEntityRenderer */
    public static void onUpdateCameraAndRender(float par) {
        ZModpack.guiDraw(par);
    }

    public static void beginRenderRainSnow(float par) {
        ZModpack.worldDraw(par);
    }

    public static void endRenderRainSnow(float par) {
        // DNAA
    }

    public static boolean forwardRenderRainSnow() {
        // return !optWeatherNoDraw;
        return true;
    }

    /* ZRenderGlobal */

    /*
    public static void onLoadRenderers(boolean flag) {
        ZModpack.onLoadRenderers(flag);
        //ZModpack.itemGraphicsLevelHandle(mc.gameSettings.fancyGraphics);
    }
    */

    /* EntityPlayer */


    /* EntityPlayerSP */

    /* EntityPlayerMP */

    /* EnumGameType */

    /* MovementInputFromOptions */

    /* NetHandlerPlayClient */

    public static void onNetworkTick(EntityPlayerSP e) {
        //ZModpack.onClientTick(e);
    }

    /* NetHandlerPlayServer */

    public static void onNetworkTick(EntityPlayerMP e) {
        if (ZWrapper.isServerPlayer(e)) {
            ZModpack.serverTick(e);
        }
    }

    /* WorldProvider */



}
