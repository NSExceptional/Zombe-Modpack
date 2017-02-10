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

    private boolean optCheckReach, optCheckRaytrace, optSyncDigged;
    private boolean optReachSet, optReachSetDig, optReachSetUse, optReachSetPlace;
    private double optReach, optReachDig, optReachUse, optReachPlace;
    private int optBlockHitDelay = 5;

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
    private Object onPlayerRaytrace(Object arg) {
        if (arg instanceof RayTraceResult) {
            return this.onRaytrace(getPlayer(), (RayTraceResult) arg);
        }

        return arg;
    }

    @Nullable
    private Object onViewRaytrace(Object arg) {
        Entity view = getView();
        assert view != null;

        if (arg instanceof RayTraceResult) {
            return this.onRaytrace(view, (RayTraceResult) arg);
        }

        return arg;
    }

    @Nullable
    private RayTraceResult onRaytrace(Entity src, RayTraceResult mop) {
        if (this.optCheckRaytrace) {
            BlockFace bf = getBlockFace(mop);
            Entity ent = getEntity(mop);
            if (ent == src || (ent == null || !this.checkReachUse(ent)) && (bf == null || (!this.checkReachDig(bf) && !this.checkReachPlace(bf)))) {
                return getMOP();
            }
        }

        return mop;
    }

    private int getBlockHitDelay(int defaultValue) {
        return this.optBlockHitDelay;
    }

    private double getReach(double reach) {
        if (this.optReachSet) {
            reach = this.optReach;
            if (isCreative(getPlayer())) {
                reach += 0.5;
            }
        }

        return reach;
    }

    private double getReachSq(double defaultValue) {
        return this.getThingSquared(this.getReach(0), defaultValue);
    }

    private double getReachUse(double reach) {
        if (!isMultiplayer() && this.optReachSetUse) {
            return this.optReachUse;
        }

        return reach;
    }

    private double getReachUseSq(double defaultValue) {
        return this.getThingSquared(this.getReachUse(0), defaultValue);
    }

    private double getReachDig(double reach) {
        if (!isMultiplayer() && this.optReachSetDig) {
            return this.optReachDig;
        }

        return reach;
    }

    private double getReachDigSq(double defaultValue) {
        return this.getThingSquared(this.getReachDig(0), defaultValue);
    }

    private double getReachPlace(double reach) {
        if (!isMultiplayer() && this.optReachSetPlace) {
            return this.optReachPlace;
        }

        return reach;
    }

    private double getReachPlaceSq(double defaultValue) {
        return this.getThingSquared(this.getReachPlace(0), defaultValue);
    }

    private double getThingSquared(double thing, double defaultValue) {
        return thing == 0 ? defaultValue : thing * thing;
    }

    private boolean checkReachUse(@Nullable Entity ent) {
        if (!this.optCheckReach) {
            return true;
        }

        EntityPlayer player = getPlayer();
        if (player == null || ent == null || player == ent) {
            return false;
        }

        boolean seen = player.canEntityBeSeen(ent);
        double reach = seen ? this.getReachUseSq(36) : 9f;
        double dx = player.posX - ent.posX;
        double dy = player.posY - ent.posY;
        double dz = player.posZ - ent.posZ;
        double dist = dx * dx + dy * dy + dz * dz;
        return dist < reach;
    }

    private boolean checkReachDig(@Nullable BlockFace bf) {
        if (!this.optCheckReach) {
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
        return dist <= this.getReachDigSq(36);
    }

    private boolean checkReachPlace(@Nullable BlockFace bf) {
        if (!this.optCheckReach) {
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
        return dist < this.getReachPlaceSq(64);
    }

    /*
    private Packet makeBlockRequestPacket(int x, int y, int z) {
        return new Packet14BlockDig(3, x,y,z, -1);
    }

    private void askBlockInfo(int x, int y, int z) {
        queuePacket(makeBlockRequestPacket(x,y,z));
    }
    */

    // TODO fix this
    private void askBlockInfo(int x, int y, int z) {
        // DNAA. broken feature as of 1.4.6
    }

    public void onBlockDigged(int x, int y, int z, int side) {
        if (ZWrapper.getPlayer() == null) {
            return;
        }

        if (this.optSyncDigged) {
            this.askBlockInfo(x, y, z);
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        switch (name) {
            case "onPlayerRayTrace":
                return this.onPlayerRaytrace(arg);
            case "onViewRayTrace":
                return this.onViewRaytrace(arg);
            case "getBlockHitDelay":
                return this.getBlockHitDelay((Integer) arg);
            case "getPlayerReach":
                return this.getReach((Double) arg);
            case "getPlayerReachSq":
                return this.getReachSq((Double) arg);
            case "getPlayerReachUse":
                return this.getReachUse((Double) arg);
            case "getPlayerReachUseSq":
                return this.getReachUseSq((Double) arg);
            case "getPlayerReachDig":
                return this.getReachDig((Double) arg);
            case "getPlayerReachDigSq":
                return this.getReachDigSq((Double) arg);
            case "getPlayerReachPlace":
                return this.getReachPlace((Double) arg);
            case "getPlayerReachPlaceSq":
                return this.getReachPlaceSq((Double) arg);
            case "checkReachUse":
                return this.checkReachUse((Entity) arg);
            case "checkReachDig":
                return this.checkReachDig((BlockFace) arg);
            case "checkReachPlace":
                return this.checkReachPlace((BlockFace) arg);

            default:
                return arg;
        }
    }

    @Override
    protected void updateConfig() {
        this.optCheckReach = getOptionBool("optDigCheckReach");
        this.optCheckRaytrace = getOptionBool("optDigCheckRaytrace");
        this.optSyncDigged = false; // broken as of 1.4.6, they removed the BlockDig Request-status (3)
        this.optReachSet = getOptionBool("optDigReachSet");
        this.optReach = getOptionFloat("optDigReach");
        this.optReachSetUse = getOptionBool("optDigReachSetUse");
        this.optReachUse = getOptionFloat("optDigReachUse");
        this.optReachSetDig = getOptionBool("optDigReachSetDig");
        this.optReachDig = getOptionFloat("optDigReachDig");
        this.optReachSetPlace = getOptionBool("optDigReachSetPlace");
        this.optReachPlace = getOptionFloat("optDigReachPlace");
        this.optBlockHitDelay = getOptionInt("optDigBlockHitDelay");
    }
}
