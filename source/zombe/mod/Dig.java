package zombe.mod;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import zombe.core.ZMod;
import zombe.core.ZWrapper;
import zombe.core.util.BlockFace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Dig extends ZMod {

    private static boolean optCheckReach, optCheckRaytrace, optSyncDigged;
    private static boolean optReachSet, optReachSetDig, optReachSetUse, optReachSetPlace;
    private static float optReach, optReachDig, optReachUse, optReachPlace;
    private static int optBlockHitDelay = 5;

    public Dig() {
        super("dig", "1.8", "9.0.2");
        this.registerHandler("onPlayerRayTrace");
        this.registerHandler("onViewRayTrace");
        this.registerHandler("getBlockHitDelay");
        this.registerHandler("getPlayerReach");
        this.registerHandler("getPlayerReachSq");
        this.registerHandler("getPlayerReachUse");
        this.registerHandler("getPlayerReachUseSq");
        this.registerHandler("getPlayerReachDig");
        this.registerHandler("getPlayerReachDigSq");
        this.registerHandler("getPlayerReachPlace");
        this.registerHandler("getPlayerReachPlaceSq");
        this.registerHandler("checkReachUse");
        this.registerHandler("checkReachDig");
        this.registerHandler("checkReachPlace");

        this.addOption("optDigBlockHitDelay", "Block hit delay. Lower is faster and buggier", 5, 0, 5);
        this.addOption("optDigCheckReach", "Cancel out of reach actions", true);
        this.addOption("optDigCheckRaytrace", "Cancel out of reach raytraces", false);
        //optDigSyncDigged = addOption(optDigSyncDigged, "optDigSyncDigged", false, "Synchronize newly digged blocks");
        this.addOption("optDigReachSet", "Set arm reach (raytrace & block outline)", true);
        this.addOption("optDigReach", "Arm reach (capped to ~8 in MP)", 4.5f, 2f, 64f, true);
        this.addOption("singleplayer-only features");
        this.addOption("optDigReachSetUse", "Set max reach for using entities", false);
        this.addOption("optDigReachUse", "Use reach (default 6)", 6f, 2f, 64f, true);
        this.addOption("optDigReachSetDig", "Set max reach for digging blocks", false);
        this.addOption("optDigReachDig", "Dig reach (default 6)", 6f, 2f, 64f, true);
        this.addOption("optDigReachSetPlace", "Set max reach for placing blocks", false);
        this.addOption("optDigReachPlace", "Place reach (default 8)", 8f, 2f, 64f, true);
    }

    @Nullable
    private static Object onPlayerRaytrace(Object arg) {
        if (arg instanceof RayTraceResult) {
            return onRaytrace(getPlayer(), (RayTraceResult) arg);
        }
        return arg;
    }

    @Nullable
    private static Object onViewRaytrace(Object arg) {
        if (arg instanceof RayTraceResult) {
            return onRaytrace(getView(), (RayTraceResult) arg);
        }
        return arg;
    }

    @Nullable
    private static RayTraceResult onRaytrace(Entity src, RayTraceResult mop) {
        if (optCheckRaytrace) {
            BlockFace bf = getBlockFace(mop);
            Entity ent = getEntity(mop);
            if (ent == src) {
                return getMOP();
            }
            if (ent != null && checkReachUse(ent) || bf != null && (checkReachDig(bf) || checkReachPlace(bf))) {
                return mop;
            }
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
            if (isCreative(getPlayer())) {
                reach += 0.5;
            }
        }
        return reach;
    }

    private static float getReachSq(float def) {
        float reach = getReach(0);
        return reach == 0 ? def : reach * reach;
    }

    private static float getReachUse(float reach) {
        if (!isMultiplayer() && optReachSetUse) {
            return optReachUse;
        }
        return reach;
    }

    private static float getReachUseSq(float def) {
        float reach = getReachUse(0);
        return reach == 0 ? def : reach * reach;
    }

    private static float getReachDig(float reach) {
        if (!isMultiplayer() && optReachSetDig) {
            return optReachDig;
        }
        return reach;
    }

    private static float getReachDigSq(float def) {
        float reach = getReachDig(0);
        return reach == 0 ? def : reach * reach;
    }

    private static float getReachPlace(float reach) {
        if (!isMultiplayer() && optReachSetPlace) {
            return optReachPlace;
        }
        return reach;
    }

    private static float getReachPlaceSq(float def) {
        float reach = getReachPlace(0);
        return reach == 0 ? def : reach * reach;
    }

    private static boolean checkReachUse(@Nullable Entity ent) {
        if (!optCheckReach) {
            return true;
        }
        EntityPlayer player = getPlayer();
        if (player == null || ent == null || player == ent) {
            return false;
        }
        boolean seen = player.canEntityBeSeen(ent);
        float reach = seen ? getReachUseSq(36) : 9f;
        double dx = player.posX - ent.posX;
        double dy = player.posY - ent.posY;
        double dz = player.posZ - ent.posZ;
        double dist = dx * dx + dy * dy + dz * dz;
        return dist < reach;
    }

    private static boolean checkReachDig(@Nullable BlockFace bf) {
        if (!optCheckReach) {
            return true;
        }
        EntityPlayer player = getPlayer();
        if (player == null || bf == null) {
            return false;
        }
        double dx = player.posX - (bf.x + 0.5);
        double dy = player.posY - (bf.y + 0.5) + 1.5;
        double dz = player.posZ - (bf.z + 0.5);
        double dist = dx * dx + dy * dy + dz * dz;
        return dist <= getReachDigSq(36);
    }

    private static boolean checkReachPlace(@Nullable BlockFace bf) {
        if (!optCheckReach) {
            return true;
        }
        EntityPlayer player = getPlayer();
        if (player == null || bf == null) {
            return false;
        }
        double dx = player.posX - (bf.x + 0.5);
        double dy = player.posY - (bf.y + 0.5);
        double dz = player.posZ - (bf.z + 0.5);
        double dist = dx * dx + dy * dy + dz * dz;
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
        if (ZWrapper.getPlayer() == null) {
            return;
        }
        if (optSyncDigged) {
            askBlockInfo(x, y, z);
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("onPlayerRayTrace")) {
            return onPlayerRaytrace(arg);
        }
        if (name.equals("onViewRayTrace")) {
            return onViewRaytrace(arg);
        }
        if (name.equals("getBlockHitDelay")) {
            return getBlockHitDelay((Integer) arg);
        }
        if (name.equals("getPlayerReach")) {
            return getReach((Float) arg);
        }
        if (name.equals("getPlayerReachSq")) {
            return getReachSq((Float) arg);
        }
        if (name.equals("getPlayerReachUse")) {
            return getReachUse((Float) arg);
        }
        if (name.equals("getPlayerReachUseSq")) {
            return getReachUseSq((Float) arg);
        }
        if (name.equals("getPlayerReachDig")) {
            return getReachDig((Float) arg);
        }
        if (name.equals("getPlayerReachDigSq")) {
            return getReachDigSq((Float) arg);
        }
        if (name.equals("getPlayerReachPlace")) {
            return getReachPlace((Float) arg);
        }
        if (name.equals("getPlayerReachPlaceSq")) {
            return getReachPlaceSq((Float) arg);
        }
        if (name.equals("checkReachUse")) {
            return checkReachUse((Entity) arg);
        }
        if (name.equals("checkReachDig")) {
            return checkReachDig((BlockFace) arg);
        }
        if (name.equals("checkReachPlace")) {
            return checkReachPlace((BlockFace) arg);
        }
        return arg;
    }

    @Override
    protected void updateConfig() {
        optCheckReach    = getOptionBool("optDigCheckReach");
        optCheckRaytrace = getOptionBool("optDigCheckRaytrace");
        optSyncDigged    = false; // broken as of 1.4.6, they removed the BlockDig Request-status (3)
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
}
