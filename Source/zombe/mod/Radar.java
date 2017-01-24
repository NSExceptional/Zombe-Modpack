package zombe.mod;

import net.minecraft.client.entity.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;

import static zombe.core.ZWrapper.*;
import zombe.core.*;
import java.lang.*;
import java.util.*;
import org.lwjgl.input.Keyboard;

public final class Radar extends ZMod {

    private static int keyToggle;
    private static String optPrefixNear, optPrefixFar;
    private static boolean optRadar, optShowDirection, optShowCompass;
    private static float optRangeMax = 1000, optRangeNear = 1;

    private static boolean radarShow;
    
    public Radar() {
        super("radar", "1.8", "9.0.2");
        
        addOption("keyRadar", "Toggle radar", Keyboard.KEY_F4);
        addOption("optRadar", "Radar enabled by default", true);
        addOption("optRadarShowCompass", "Show players compass on radar", true);
        addOption("optRadarShowDirection", "Show players direction on radar", false);
        addOption("optRadarRangeNear", "Player 'near' range", 50f, 2f, 200f, true);
        addOption("optRadarRangeMax", "Player 'far' range", 500f, 10f, 1000f, true);
        addOption("optRadarPrefixNear", "'near' color prefix", "b");
        addOption("optRadarPrefixFar", "'far' color prefix", "9");
    }
    
    @Override
    protected void init() {
        radarShow = optRadar;
    }
    
    @Override
    protected void quit() {
        setMessage("radar", null);
    }

    @Override
    protected void updateConfig() {
        keyToggle          = getOptionKey("keyRadar");
        optRadar           = getOptionBool("optRadar");
        optShowCompass     = getOptionBool("optRadarShowCompass");
        optShowDirection   = getOptionBool("optRadarShowDirection");
        optRangeNear       = getOptionFloat("optRadarRangeNear");
        optRangeMax        = getOptionFloat("optRadarRangeMax");
        optPrefixNear      = getOptionString("optRadarPrefixNear");
        optPrefixFar       = getOptionString("optRadarPrefixFar");
    }
    
    @Override
    protected void onClientTick(EntityPlayerSP player) {
        List list = getEntities();
        if (!isInMenu() && wasKeyPressedThisTick(keyToggle))
            radarShow = !radarShow;
        setMessage("radar", null);
        Entity view = getView();
        if (view == null) view = player;
        double px = getX(player), py = getY(player), pz = getZ(player);
        double vx = getX(view),   vy = getY(view),   vz = getZ(view);
        
        if (radarShow && (!isInMenu() || getMenu() instanceof GuiChat
            || getMenu() instanceof GuiContainer)) {
            double mX, mY, mZ, distp, distv;
            final double rangemax = optRangeMax*optRangeMax;
            String radar = "";
            Vec3 lookp = getLookVector(player, 1f);
            Vec3 lookv = getLookVector(view, 1f);
            for (Object obj : list) {
                if (!(obj instanceof EntityPlayer)
                 || obj == view && player == view)
                    continue;
                EntityPlayer ent = (EntityPlayer)obj;
                mX = getX(ent) - px;
                mY = getY(ent) - py;
                mZ = getZ(ent) - pz;
                distp = mX*mX + mY*mY + mZ*mZ;
                String dirp = getFineFacingName(mX, mY, mZ);
                String comp = getRelativeCompass(mX,mZ,getX(lookp),getZ(lookp));
                mX = getX(ent) - vx;
                mY = getY(ent) - vy;
                mZ = getZ(ent) - vz;
                distv = mX*mX + mY*mY + mZ*mZ;
                if (distp > rangemax && distv > rangemax) continue;
                String dirv = getFineFacingName(mX, mY, mZ);
                String comv = getRelativeCompass(mX,mZ,getX(lookv),getZ(lookv));
                distp = Math.sqrt(distp);
                distv = Math.sqrt(distv);
                String partp = "\u00a7b" + ((int)distp) + "\u00a7fm"
                             + (optShowCompass 
                             ? " [\u00a7b" + comp + "\u00a7f]" : "")
                             + (optShowDirection
                             ? " \u00a7b" + dirp : "");
                String partv = (player == view) ? "" 
                             : " \u00a7f/ \u00a7b" + ((int)distv) + "\u00a7fm"
                             + (optShowCompass 
                             ? " [\u00a7b" + comv + "\u00a7f]" : "")
                             + (optShowDirection
                             ? " \u00a7b" + dirv : "");
                radar += "\u00a7" + (distp < optRangeNear 
                      ?  optPrefixNear : optPrefixFar)
                      +  ZWrapper.getName(ent) + " \u00a7f("
                      +  partp + partv + "\u00a7f)\n";
            }
            if (radar.length() == 0) radar = "\u00a7fno players nearby";
            setMessage("radar", radar);
        }
    }

}
