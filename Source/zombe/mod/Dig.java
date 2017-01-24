package zombe.mod;

import net.minecraft.client.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;

import static zombe.core.ZWrapper.*;
import zombe.core.*;
import zombe.core.util.*;
import java.lang.*;
import org.lwjgl.input.Keyboard;

public final class Dig extends ZMod {

    private static boolean optCheckReach, optCheckRaytrace, optSyncDigged;
    private static boolean optReachSet, optReachSetDig, optReachSetUse, optReachSetPlace;
    private static float optReach, optReachDig, optReachUse, optReachPlace;
    private static int optBlockHitDelay = 5;

    public Dig() {
        super("dig", "1.8", "9.0.2");
        registerHandler("onPlayerRayTrace");
        registerHandler("onViewRayTrace");
        registerHandler("getBlockHitDelay");
        registerHandler("getPlayerReach");
        registerHandler("getPlayerReachSq");
        registerHandler("getPlayerReachUse");
        registerHandler("getPlayerReachUseSq");
        registerHandler("getPlayerReachDig");
        registerHandler("getPlayerReachDigSq");
        registerHandler("getPlayerReachPlace");
        registerHandler("getPlayerReachPlaceSq");
        registerHandler("checkReachUse");
        registerHandler("checkReachDig");
        registerHandler("checkReachPlace");

        addOption("optDigBlockHitDelay", "Block hit delay. Lower is faster and buggier", 5, 0, 5);
        addOption("optDigCheckReach", "Cancel out of reach actions", true);
        addOption("optDigCheckRaytrace", "Cancel out of reach raytraces", false);
        //optDigSyncDigged = addOption(optDigSyncDigged, "optDigSyncDigged", false, "Synchronize newly digged blocks");
        addOption("optDigReachSet", "Set arm reach (raytrace & block outline)", true);
        addOption("optDigReach", "Arm reach (capped to ~8 in MP)", 4.5f, 2f, 64f, true);
        addOption("singleplayer-only features");
        addOption("optDigReachSetUse", "Set max reach for using entities", false);
        addOption("optDigReachUse", "Use reach (default 6)", 6f, 2f, 64f, true);
        addOption("optDigReachSetDig", "Set max reach for digging blocks", false);
        addOption("optDigReachDig", "Dig reach (default 6)", 6f, 2f, 64f, true);
        addOption("optDigReachSetPlace", "Set max reach for placing blocks", false);
        addOption("optDigReachPlace", "Place reach (default 8)", 8f, 2f, 64f, true);
    }
    
    @Override
    protected void updateConfig() {
        optCheckReach    = getOptionBool("optDigCheckReach");
        optCheckRaytrace = getOptionBool("optDigCheckRaytrace");
        optSyncDigged = false; // broken as of 1.4.6, they removed the BlockDig Request-status (3)
        optReachSet      = getOptionBool("optDigReachSet");
        optReach         = getOptionFloat("optDigReach");
        optReachSetUse   = getOptionBool("optDigReachSetUse");
        optReachUse      = getOptionFloat("optDigReachUse");
        optReachSetDig   = getOptionBool("optDigReachSetDig");
        optReachDig      = getOptionFloat("optDigReachDig");
        optReachSetPlace = getOptionBool("optDigReachSetPlace");
        optReachPlace    = getOptionFloat("optDigReachPlace");
        optBlockHitDelay = getOptionInt("optDigBlockHitDelay");
    }

    @Override
    protected Object handle(String name, Object arg) {
        if (name == "onPlayerRayTrace")
            return onPlayerRaytrace(arg);
        if (name == "onViewRayTrace")
            return onViewRaytrace(arg);
        if (name == "getBlockHitDelay")
            return (Integer) getBlockHitDelay((Integer) arg);
        if (name == "getPlayerReach")
            return (Float) getReach((Float) arg);
        if (name == "getPlayerReachSq")
            return (Float) getReachSq((Float) arg);
        if (name == "getPlayerReachUse")
            return (Float) getReachUse((Float) arg);
        if (name == "getPlayerReachUseSq")
            return (Float) getReachUseSq((Float) arg);
        if (name == "getPlayerReachDig")
            return (Float) getReachDig((Float) arg);
        if (name == "getPlayerReachDigSq")
            return (Float) getReachDigSq((Float) arg);
        if (name == "getPlayerReachPlace")
            return (Float) getReachPlace((Float) arg);
        if (name == "getPlayerReachPlaceSq")
            return (Float) getReachPlaceSq((Float) arg);
        if (name == "checkReachUse")
            return (Boolean) checkReachUse((Entity) arg);
        if (name == "checkReachDig")
            return (Boolean) checkReachDig((BlockFace) arg);
        if (name == "checkReachPlace")
            return (Boolean) checkReachPlace((BlockFace) arg);
        return arg;
    }

    private static Object onPlayerRaytrace(Object arg) {
        if (arg instanceof MovingObjectPosition)
            return onRaytrace(getPlayer(), (MovingObjectPosition) arg);
        return arg;
    }

    private static Object onViewRaytrace(Object arg) {
        if (arg instanceof MovingObjectPosition)
            return onRaytrace(getView(), (MovingObjectPosition) arg);
        return arg;
    }

    private static MovingObjectPosition onRaytrace(Entity src, 
        MovingObjectPosition mop) {
        if (optCheckRaytrace) {
            BlockFace bf = getBlockFace(mop);
            Entity ent = getEntity(mop);
            if (ent == src) return getMOP();
            if (ent != null && checkReachUse(ent)
             || bf != null && (checkReachDig(bf) || checkReachPlace(bf)))
                return mop;
            return getMOP();
        }
        return mop;
    }

    private static int getBlockHitDelay(int def) {
        return optBlockHitDelay;
    }

    private static float getReach(float reach) {
        if (optReachSet) {
            reach = optReach;
            if (isCreative(getPlayer()))
                reach += 0.5;
        }
        return reach;
    }
    private static float getReachSq(float def) {
        float reach = getReach(0);
        return reach == 0 ? def : reach*reach;
    }

    private static float getReachUse(float reach) {
        if (!isMultiplayer() && optReachSetUse) {
            return optReachUse;
        }
        return reach;
    }
    private static float getReachUseSq(float def) {
        float reach = getReachUse(0);
        return reach == 0 ? def : reach*reach;
    }

    private static float getReachDig(float reach) {
        if (!isMultiplayer() && optReachSetDig) {
            return optReachDig;
        }
        return reach;
    }
    private static float getReachDigSq(float def) {
        float reach = getReachDig(0);
        return reach == 0 ? def : reach*reach;
    }

    private static float getReachPlace(float reach) {
        if (!isMultiplayer() && optReachSetPlace) {
            return optReachPlace;
        }
        return reach;
    }
    private static float getReachPlaceSq(float def) {
        float reach = getReachPlace(0);
        return reach == 0 ? def : reach*reach;
    }

    private static boolean checkReachUse(Entity ent) {
        if (!optCheckReach) return true;
        EntityPlayer player = getPlayer();
        if (player == null || ent == null || player == ent) return false;
        boolean seen = player.canEntityBeSeen(ent);
        float reach = seen ? getReachUseSq(36) : 9f;
        double dx = player.posX - ent.posX;
        double dy = player.posY - ent.posY;
        double dz = player.posZ - ent.posZ;
        double dist = dx*dx + dy*dy + dz*dz;
        return dist < reach;
    }

    private static boolean checkReachDig(BlockFace bf) {
        if (!optCheckReach) return true;
        EntityPlayer player = getPlayer();
        if (player == null || bf == null) return false;
        double dx = player.posX - (bf.x + 0.5);
        double dy = player.posY - (bf.y + 0.5) + 1.5;
        double dz = player.posZ - (bf.z + 0.5);
        double dist = dx*dx + dy*dy + dz*dz;
        return dist <= getReachDigSq(36);
    }

    private static boolean checkReachPlace(BlockFace bf) {
        if (!optCheckReach) return true;
        EntityPlayer player = getPlayer();
        if (player == null || bf == null) return false;
        double dx = player.posX - (bf.x + 0.5);
        double dy = player.posY - (bf.y + 0.5);
        double dz = player.posZ - (bf.z + 0.5);
        double dist = dx*dx + dy*dy + dz*dz;
        return dist < getReachPlaceSq(64);
    }

    /*
    private static Packet makeBlockRequestPacket(int x, int y, int z) {
        return new Packet14BlockDig(3, x,y,z, -1);
    }

    private static void askBlockInfo(int x, int y, int z) {
        queuePacket(makeBlockRequestPacket(x,y,z));
    }
    */
    private static void askBlockInfo(int x, int y, int z) {
        // DNAA. broken feature as of 1.4.6
    }

    public static void onBlockDigged(int x, int y, int z, int side) {
        if (ZWrapper.getPlayer() == null) return;
        if (optSyncDigged) {
            askBlockInfo(x,y,z);
        }
    }

}
