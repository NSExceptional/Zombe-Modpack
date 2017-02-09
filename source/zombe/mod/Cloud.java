package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;
import zombe.core.gui.Keys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Cloud extends ZMod {

    private String tagVanilla, tagNone, tagOffset;
    private int keyToggle, keyUp, keyDown, keyVanilla;
    private boolean optShow;
    private double optOffset;

    @Nullable static Entity view = null;
    private boolean vanillaShow, cloudShow, cloudVanilla;
    private double viewY;
    private double viewPrevY;
    private double viewLastY;

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

    private boolean getCloudSetting() {
        return getGameSettings().shouldRenderClouds() > 0;
    }

    /** Defaults to "fast" (off=0, fast=1, normal=2) */
    private void setCloudSetting(boolean clouds) {
        getGameSettings().clouds = clouds ? 1 : 0;
    }

    private double getCloudHeight(double def) {
        return this.cloudVanilla ? def : (def + this.optOffset);
    }

    private boolean beforeRenderClouds(double delta) {
        if (!this.cloudVanilla && view == null) {
            view = getView();

            if (this.cloudShow && view != null) {
                this.viewY = getY(view);
                this.viewPrevY = getPrevY(view);
                this.viewLastY = getLastY(view);
                setY(view, this.viewY - this.optOffset);
                setPrevY(view, this.viewPrevY - this.optOffset);
                setLastY(view, this.viewLastY - this.optOffset);

                return true;
            }

            return false;
        }

        return true;
    }

    private void afterRenderClouds(double delta) {
        if (!this.cloudVanilla) {
            if (this.cloudShow && view != null) {
                setY(view, this.viewY);
                setPrevY(view, this.viewPrevY);
                setLastY(view, this.viewLastY);
            }

            view = null;
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("getCloudHeight")) {
            return this.getCloudHeight((Double) arg);
        }
        /*if (name.equals("beforeRenderClouds"))
            return (Boolean) beforeRenderClouds((Double) arg);
        if (name.equals("afterRenderClouds"))
            afterRenderClouds((Double) arg);*/
        return arg;
    }

    @Override
    protected void init() {
        if (getGameSettings() != null) {
            this.vanillaShow = this.getCloudSetting();
            this.setCloudSetting(this.cloudVanilla = this.cloudShow = this.optShow);
        }
    }

    @Override
    protected void quit() {
        if (getGameSettings() != null) {
            this.setCloudSetting(this.vanillaShow);
        }
    }

    @Override
    protected void updateConfig() {
        this.tagVanilla = getOptionString("tagCloudVanilla");
        this.tagNone    = getOptionString("tagCloudNone");
        this.tagOffset  = getOptionString("tagCloudOffset");

        this.keyToggle  = getOptionKey("keyCloudToggle");
        this.optShow    = getOptionBool("optCloudShow");
        this.keyVanilla = getOptionKey("keyCloudVanilla");
        this.keyUp      = getOptionKey("keyCloudUp");
        this.keyDown    = getOptionKey("keyCloudDown");
        this.optOffset  = getOptionFloat("optCloudOffset");
    }

    @Override
    protected void onWorldChange() {
        this.vanillaShow = this.getCloudSetting();
        this.setCloudSetting(this.cloudVanilla = this.cloudShow = this.optShow);
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        if (isInMenu()) {
            return;
        }

        if (this.cloudShow != this.getCloudSetting()) {
            this.vanillaShow = this.getCloudSetting();
        }

        int keyPressed = Keys.getKeyPressedThisTick();

        if (keyPressed == this.keyVanilla) {
            this.cloudVanilla = !this.cloudVanilla;
            if (this.cloudVanilla) {
                this.setCloudSetting(this.vanillaShow);
            }
        } else {
            if (!this.cloudVanilla) {
                if (keyPressed == this.keyToggle) {
                    this.cloudShow = !this.cloudShow;
                }
                if (keyPressed == this.keyUp) {
                    this.optOffset += 1f;
                }
                if (keyPressed == this.keyDown) {
                    this.optOffset -= 1f;
                }
            } else {
                this.cloudVanilla = false;
            }
        }

        if (!this.cloudVanilla) {
            this.setCloudSetting(this.cloudShow);
        }
    }

    @Nullable
    @Override
    protected String getTag() {
        // Do not merge if statements, this method is logically simplified
        
        if (this.cloudVanilla) {
            if (!this.tagVanilla.isEmpty()) {
                return this.tagVanilla;
            }
        } else {
            if (this.cloudShow) {
                if (!this.tagOffset.isEmpty() && this.optOffset != 0f) {
                    return this.tagOffset + (this.optOffset > 0 ? "+" : "") + this.optOffset;
                }
            } else if (!this.tagNone.isEmpty()) {
                return this.tagNone;

            }
        }

        return null;
    }
}
