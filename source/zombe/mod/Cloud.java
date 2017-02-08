package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Cloud extends ZMod {

    private static String tagVanilla, tagNone, tagOffset;
    private static int keyToggle, keyUp, keyDown, keyVanilla;
    private static boolean optShow;
    private static float optOffset;

    @Nullable private static Entity view = null;
    private static boolean vanillaShow, cloudShow, cloudVanilla;
    private static double viewY;
    private static double viewPrevY;
    private static double viewLastY;

    public Cloud() {
        super("cloud", "1.8", "9.0.1");
        this.registerHandler("getCloudHeight");
        this.registerListener("beforeRenderClouds");
        this.registerListener("afterRenderClouds");

        this.addOption("tagCloudVanilla", "Tag shown for vanilla clouds", "");
        this.addOption("tagCloudNone", "Tag shown for no clouds", "no-cloud");
        this.addOption("tagCloudOffset", "Tag shown for custom height", "cloud");

        this.addOption("keyCloudToggle", "Toggle clouds", Keyboard.KEY_MULTIPLY);
        this.addOption("optCloudShow", "Show clouds by default", true);
        this.addOption("keyCloudVanilla", "Toggle vanilla clouds", Keyboard.KEY_V);
        this.addOption("keyCloudUp", "Move clouds up", Keyboard.KEY_NONE);
        this.addOption("keyCloudDown", "Move clouds down", Keyboard.KEY_NONE);
        this.addOption("optCloudOffset", "Cloud offset", 4f, -60f, 140f);
    }

    private static boolean getCloudSetting() {
        return getGameSettings().shouldRenderClouds() > 0;
    }

    /** Defaults to "fast" (off=0, fast=1, normal=2) */
    private static void setCloudSetting(boolean clouds) {
        getGameSettings().clouds = clouds ? 1 : 0;
    }

    private static float getCloudHeight(float def) {
        return (cloudVanilla) ? def : def + optOffset;
    }

    private static boolean beforeRenderClouds(float delta) {
        if (!cloudVanilla && view == null) {
            view = getView();
            if (cloudShow && view != null) {
                viewY = getY(view);
                viewPrevY = getPrevY(view);
                viewLastY = getLastY(view);
                setY(view, viewY - optOffset);
                setPrevY(view, viewPrevY - optOffset);
                setLastY(view, viewLastY - optOffset);
                return true;
            }
            return false;
        }
        return true;
    }

    private static void afterRenderClouds(float delta) {
        if (!cloudVanilla) {
            if (cloudShow && view != null) {
                setY(view, viewY);
                setPrevY(view, viewPrevY);
                setLastY(view, viewLastY);
            }
            view = null;
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("getCloudHeight")) {
            return getCloudHeight((Float) arg);
        }
        /*if (name.equals("beforeRenderClouds"))
            return (Boolean) beforeRenderClouds((Float) arg);
        if (name.equals("afterRenderClouds"))
            afterRenderClouds((Float) arg);*/
        return arg;
    }

    @Override
    protected void init() {
        if (getGameSettings() != null) {
            vanillaShow = getCloudSetting();
            setCloudSetting(cloudVanilla = cloudShow = optShow);
        }
    }

    @Override
    protected void quit() {
        if (getGameSettings() != null) {
            setCloudSetting(vanillaShow);
        }
    }

    @Override
    protected void updateConfig() {
        tagVanilla = getOptionString("tagCloudVanilla");
        tagNone    = getOptionString("tagCloudNone");
        tagOffset  = getOptionString("tagCloudOffset");

        keyToggle  = getOptionKey("keyCloudToggle");
        optShow    = getOptionBool("optCloudShow");
        keyVanilla = getOptionKey("keyCloudVanilla");
        keyUp      = getOptionKey("keyCloudUp");
        keyDown    = getOptionKey("keyCloudDown");
        optOffset  = getOptionFloat("optCloudOffset");
    }

    @Override
    protected void onWorldChange() {
        vanillaShow = getCloudSetting();
        setCloudSetting(cloudVanilla = cloudShow = optShow);
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        if (isInMenu()) {
            return;
        }
        if (cloudShow != getCloudSetting()) {
            vanillaShow = getCloudSetting();
        }
        if (wasKeyPressedThisTick(keyVanilla)) {
            cloudVanilla = !cloudVanilla;
            if (cloudVanilla) {
                setCloudSetting(vanillaShow);
            }
        }
        if (wasKeyPressedThisTick(keyToggle)) {
            if (cloudVanilla) {
                cloudVanilla = false;
            } else {
                cloudShow = !cloudShow;
            }
        }
        if (wasKeyPressedThisTick(keyUp)) {
            if (cloudVanilla) {
                cloudVanilla = false;
            } else {
                optOffset += 1f;
            }
        }
        if (wasKeyPressedThisTick(keyDown)) {
            if (cloudVanilla) {
                cloudVanilla = false;
            } else {
                optOffset -= 1f;
            }
        }
        if (!cloudVanilla) {
            setCloudSetting(cloudShow);
        }
    }

    @Override
    protected String getTag() {
        if (cloudVanilla && tagVanilla.length() != 0) {
            return tagVanilla;
        }
        if (!cloudVanilla && !cloudShow && tagNone.length() != 0) {
            return tagNone;
        }
        if (!cloudVanilla && cloudShow && tagOffset.length() != 0 && optOffset != 0f) {
            return tagOffset + (optOffset > 0 ? "+" : "") + optOffset;
        }
        return null;
    }
}
