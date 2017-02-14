package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;
import zombe.core.ZWrapper;

import javax.annotation.Nonnull;
import java.util.List;

import static zombe.core.ZWrapper.*;

public final class Radar extends ZMod {

    private int keyToggle;
    private String optPrefixNear, optPrefixFar;
    private boolean optRadar, optShowDirection, optShowCompass;
    private float optRangeMax = 1000, optRangeNear = 1;

    private boolean radarShow;

    public Radar() {
        super("radar", "1.8", "9.0.2");

        this.addOption("keyRadar", "Toggle radar", Keyboard.KEY_F4);
        this.addOption("optRadar", "Radar enabled by default", true);
        this.addOption("optRadarShowCompass", "Show players compass on radar", true);
        this.addOption("optRadarShowDirection", "Show players direction on radar", false);
        this.addOption("optRadarRangeNear", "Player 'near' range", 50f, 2f, 200f, true);
        this.addOption("optRadarRangeMax", "Player 'far' range", 500f, 10f, 1000f, true);
        this.addOption("optRadarPrefixNear", "'near' color prefix", "b");
        this.addOption("optRadarPrefixFar", "'far' color prefix", "9");
    }

    @Override
    protected void init() {
        this.radarShow = this.optRadar;
    }

    @Override
    protected void quit() {
        setMessage("radar", null);
    }

    @Override
    protected void updateConfig() {
        this.keyToggle = getOptionKey("keyRadar");
        this.optRadar = getOptionBool("optRadar");
        this.optShowCompass = getOptionBool("optRadarShowCompass");
        this.optShowDirection = getOptionBool("optRadarShowDirection");
        this.optRangeNear = getOptionFloat("optRadarRangeNear");
        this.optRangeMax = getOptionFloat("optRadarRangeMax");
        this.optPrefixNear = getOptionString("optRadarPrefixNear");
        this.optPrefixFar = getOptionString("optRadarPrefixFar");
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        List list = getEntities();
        if (!isInMenu() && wasKeyPressedThisTick(this.keyToggle)) {
            this.radarShow = !this.radarShow;
        }

        setMessage("radar", null);
        Entity view = getView();
        if (view == null) {
            view = player;
        }

        double px = getX(player), py = getY(player), pz = getZ(player);
        double vx = getX(view), vy = getY(view), vz = getZ(view);

        if (this.radarShow && (!isInMenu() || getMenu() instanceof GuiChat || getMenu() instanceof GuiContainer)) {
            double mX, mY, mZ, distp, distv;
            final double rangemax = this.optRangeMax * this.optRangeMax;
            String radar = "";
            Vec3d lookp = getLookVector(player, 1f);
            Vec3d lookv = getLookVector(view, 1f);
            for (Object obj : list) {
                if (!(obj instanceof EntityPlayer) || obj == view && player == view) {
                    continue;
                }

                EntityPlayer ent = (EntityPlayer) obj;
                mX = getX(ent) - px;
                mY = getY(ent) - py;
                mZ = getZ(ent) - pz;
                distp = mX * mX + mY * mY + mZ * mZ;
                String dirp = getFineFacingName(mX, mY, mZ);
                String comp = getRelativeCompass(mX, mZ, getX(lookp), getZ(lookp));
                mX = getX(ent) - vx;
                mY = getY(ent) - vy;
                mZ = getZ(ent) - vz;

                distv = mX * mX + mY * mY + mZ * mZ;
                if (distp > rangemax && distv > rangemax) {
                    continue;
                }

                String dirv = getFineFacingName(mX, mY, mZ);
                String comv = getRelativeCompass(mX, mZ, getX(lookv), getZ(lookv));
                distp = Math.sqrt(distp);
                distv = Math.sqrt(distv);
                String partp = "\u00a7b" + (int)distp + "\u00a7fm" + (this.optShowCompass ? " [\u00a7b" + comp + "\u00a7f]" : "") + (
                        this.optShowDirection ? " \u00a7b" + dirp : "");
                String partv = (player == view) ? "" : " \u00a7f/ \u00a7b" + (int)distv + "\u00a7fm" + (
                        this.optShowCompass ? " [\u00a7b" + comv + "\u00a7f]" : "") + (
                        this.optShowDirection ? " \u00a7b" + dirv : "");
                radar += "\u00a7" + (distp < this.optRangeNear ? this.optPrefixNear
                                                               : this.optPrefixFar) + ZWrapper.getName(ent) + " \u00a7f(" + partp + partv + "\u00a7f)\n";
            }

            if (radar.isEmpty()) {
                radar = "\u00a7fno players nearby";
            }

            setMessage("radar", radar);
        }
    }
}
