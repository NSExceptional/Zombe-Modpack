package zombe.mod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.input.Keyboard;
import zombe.core.ZHandle;
import zombe.core.ZMod;
import zombe.core.ZWrapper;
import zombe.core.util.BlockFace;
import zombe.core.util.TimeHelper;

import java.util.List;
import java.util.Random;

import static zombe.core.ZWrapper.*;

public final class Info extends ZMod {

    private static int keyToggle;
    private static boolean optHideAchievement, optTagPos, optTagDirection, optTagCompass, optTagBiome, optTagFPS, optTagTime, optShowPos, optShowDirection, optShowTime, optShowBiome, optShowItem, optShowBlock, optShowEntity;

    private static boolean infoShow, infoDeath, infoServer;
    private static int infoDeathX, infoDeathY, infoDeathZ, infoFrames = 0;
    private static long infoTime = 0;
    private static String infoFps = "";

    public Info() {
        super("info", "1.8", "9.0.2");

        addOption("optInfoHideAchievement", "Hide the obnoxious achievements", false);
        addOption("optInfoTagPos", "Tag your coordinates", false);
        addOption("optInfoTagCompass", "Tag a compass", true);
        addOption("optInfoTagDirection", "Tag your direction", false);
        addOption("optInfoTagBiome", "Tag the biome name", false);
        addOption("optInfoTagFPS", "Tag a FPS counter", true);
        addOption("optInfoTagTime", "Tag the time", false);
        addOption("keyInfoToggle", "Toggle info screen", Keyboard.KEY_F12);
        addOption("optInfoShowPos", "Show your coordinates", false);
        addOption("optInfoShowDirection", "Show your direction", false);
        addOption("optInfoShowBiome", "Show the biome name", false);
        addOption("optInfoShowTime", "Tag the time", false);
        addOption("optInfoShowItem", "Show selected item information", true);
        addOption("optInfoShowBlock", "Show target block information", true);
        addOption("optInfoShowEntity", "Show target entity information", true);
    }

    @Override
    protected void updateConfig() {
        keyToggle          = getOptionKey("keyInfoToggle");
        optHideAchievement = getOptionBool("optInfoHideAchievement");
        optTagPos          = getOptionBool("optInfoTagPos");
        optTagCompass      = getOptionBool("optInfoTagCompass");
        optTagDirection    = getOptionBool("optInfoTagDirection");
        optTagBiome        = getOptionBool("optInfoTagBiome");
        optTagFPS          = getOptionBool("optInfoTagFPS");
        optTagTime         = getOptionBool("optInfoTagTime");
        optShowPos         = getOptionBool("optInfoShowPos");
        optShowDirection   = getOptionBool("optInfoShowDirection");
        optShowBiome       = getOptionBool("optInfoShowBiome");
        optShowTime        = getOptionBool("optInfoShowTime");
        optShowItem        = getOptionBool("optInfoShowItem");
        optShowBlock       = getOptionBool("optInfoShowBlock");
        optShowEntity      = getOptionBool("optInfoShowEntity");
    }

    @Override
    protected void init() {
        infoDeath = false;
        infoShow = false;
        infoServer = false;
    }

    @Override
    protected void quit() {
        setMessage("info", null);
    }

    @Override
    protected void onWorldChange() {
        infoServer = false;
        infoDeath = false;
    }

    @SuppressWarnings({"UnusedAssignment", "ConstantConditions"})
    @Override
    protected void onClientTick(EntityPlayerSP player) {
        List list = getEntities();
        if (!isInMenu() && wasKeyPressedThisTick(keyToggle))
            infoShow = !infoShow;
        setMessage("info", null);
        String info = "";
        Entity view = getView();
        if (view == null) view = player;
        double px = getX(player), py = getY(player), pz = getZ(player);
        double vx = getX(view),   vy = getY(view),   vz = getZ(view);
        int x = fix(vx), y = fix(vy), z = fix(vz);

        if (getHealth(player) <= 0) {
            infoDeath = true;
            infoDeathX = fix(px);
            infoDeathY = fix(py);
            infoDeathZ = fix(pz);
        }

        if (isInMenu() && !(getMenu() instanceof GuiChat)) return;

        if (!infoShow) return;

        int mx, my, mz, id, meta, cap, cnt, cx = x >> 4, cz = z >> 4;
        long timeRT = getTime(), time = ZHandle.handle("getSunOffset", timeRT);
        float val;
        BlockPos at;

        // fog & exp
        info += "\nFog: \u00a7b" + getViewDistance()
             +  "\u00a7f    Exp-orbs: \u00a7b" + player.experienceTotal;

        // light level
        if (y >= 0) info += "\nLight real=\u00a7b"
            + getRealLightLevel(x,y,z) + "\u00a7f (block=\u00a78"
            + getBlockLightLevel(x,y,z) + "\u00a7f sky=\u00a7e"
            + getSkyLightLevel(x,y,z) + "\u00a7f)";

        // chunk
        info += "\nChunk: \u00a7b"+cx+"\u00a7f,\u00a7b"+cz;

        // your location
        if (optShowPos && !optTagPos) info += "  Position: \u00a7b"
            + x + "\u00a7f, \u00a7b" + y + "\u00a7f, \u00a7b" + z;

        // biome
        if (optShowBiome && !optTagBiome) info += "  Biome: \u00a7b" + getBiomeName(x,z);
        if (infoServer) {
            // slimes
            Random rnd = new Random(getSeed() + (long)(cx * cx * 0x4c1906) + (long)(cx * 0x5ac0db) + (long)(cz * cz) * 0x4307a7L + (long)(cz * 0x5f24f) ^ 0x3ad8025f); // the silliest nonsense i have ever seen x_x
            info += "  Slimes: \u00a7b"
                 +  (rnd.nextInt(10)==0 ? "yes" : "no ");

            // stronghold
            //BlockPos stronghold = getWorld().findClosestStructure("Stronghold", x, y, z);
            BlockPos stronghold = getWorld().func_190528_a("Stronghold", new BlockPos(x, y, z), true);
            if (stronghold != null) {
                info += "\nStronghold: \u00a7b" + (mx = getX(stronghold))
                     +  "\u00a7f , \u00a7b" + getY(stronghold)
                     +  "\u00a7f , \u00a7b" + (mz = getZ(stronghold));
                mx -= x; mz -= z;
                info += "\u00a7f (\u00a7b" + (int)Math.sqrt(mx*mx + mz*mz)
                     +  "\u00a7fm)";
            }

            // spawn
            info += "\nSpawn: \u00a7b" + (mx = getSpawnX())
                 +  "\u00a7f , \u00a7b" + (my = getSpawnY())
                 +  "\u00a7f , \u00a7b" + (mz = getSpawnZ());
            mx -= x; mz -= z;
            info += "\u00a7f (\u00a7b" + (int)Math.sqrt(mx*mx + mz*mz)
                 +  "\u00a7fm)";
        }

        // player bed
        at = getBed(player);
        if (at != null) {
            info += "\nYour bed: \u00a7b" + (mx = getX(at))
                 +  "\u00a7f,\u00a7b" + (my = getY(at))
                 +  "\u00a7f,\u00a7b" + (mz = getZ(at));
            mx -= x; mz -= z;
            info += "\u00a7f (\u00a7b" + (int)Math.sqrt(mx*mx + mz*mz)
                 +  "\u00a7fm)";
        }

        // last death
        if (infoDeath) {
            info += "\nYou died:   \u00a7b" + (mx = infoDeathX)
                 +  "\u00a7f , \u00a7b" + (my = infoDeathY)
                 +  "\u00a7f , \u00a7b" + (mz = infoDeathZ);
            mx -= x; mz -= z;
            info += "\u00a7f (\u00a7b" + (int)Math.sqrt(mx*mx + mz*mz)
                 +  "\u00a7fm)";
        }

        // world name
        if (infoServer) {
            info += "\nWorld:  name=\u00a7b" + getWorldName()
                 +  "  seed=\u00a7b" + getSeed();

            info += "\nRaining: \u00a7b" + (getRaining() ? "yes" : "no");
            if (!getIsHell()) info += "\u00a7f the next \u00a7b"
                + (getRainingTime() / 20) + "\u00a7fs";
            info += "\nThunder: \u00a7b" + (getThunder() ? "yes" : "no");
            if (!getIsHell()) info += "\u00a7f the next \u00a7b"
                + (getThunderTime() / 20) + "\u00a7fs";
        }

        // time
        info += "\nWorld Age (real time): \u00a7b" + TimeHelper.getRealTime(timeRT);
        if (optShowTime) {
            info += "\nTime: \u00a7b" + TimeHelper.getTime(time);
            if (time != timeRT) info += "\u00a7f (actual time: \u00a7b"
                 +  TimeHelper.getTime(timeRT)+"\u00a7f)";
        }

        // item in hand
        ItemStack stack = getStacks(player)[getCurrentSlot(player)];
        if (optShowItem && stack != null) {
            id = getId(stack);
            meta = getMeta(stack);
            cnt = getStackSize(stack);
            Item item = getItem(id);
            info += "\nSelected item: \u00a7b" + ZWrapper.getName(item)
                 +  "\u00a7f (\u00a7b" + id
                 +  "\u00a7f/"+ (hasSubTypes(item)
                     ? "\u00a7b" + meta
                     : "(\u00a7b"+ meta +"\u00a7f)")
                 + "\u00a7f)  stack: \u00a7b" + cnt
                 + "\u00a7f/\u00a7b" + getItemMax(item);
            Block block = getBlock(item);

            info += "\nType: \u00a7b"
                 +  (block != null ? "Block" : "Item");
            if ((cap = getItemDmgCap(item)) != 0)
            info += "\u00a7f  damage: \u00a7b" + meta
                 +  "\u00a7f/\u00a7b" + cap;
            if (block != null) {
                info += "\u00a7f  id: \u00a7b" + ZWrapper.getName(block)
                     +  "\u00a7f (\u00a7b" + getId(block)
                     +  "\u00a7f?\u00a7b"+getBlockMeta(stack)
                     +  "\u00a7f)";
                info += "\nProperties:   hardness=\u00a7b" + getBlockHardness(block) + "\u00a7f  resistance=\u00a7b" + getBlockResist(block) + "\u00a7f  slip=\u00a7b" + getBlockSlip(block);
                info += "\nLight:   emission=\u00a7b" + getBlockLight(id) + "\u00a7f  opacity=\u00a7b" + (getBlockIsOpaque(id) ? "opaque" : getBlockOpacity(id) );
                info += "\nFire:   spread=\u00a7b" + getFireSpread(id) + "\u00a7f  burn=\u00a7b" + getFireBurn(id);
                Material mat = getBlockMaterial(block);
                info += "\nMaterial:\u00a7b";
                int i = 0;
                if (getIsSolid(mat)) { info += " solid"; ++i; }
                if (getIsBurnable(mat)) { info += " burnable"; ++i; }
                if (getIsReplaceable(mat)) { info += " replaceable"; ++i; }
                if (getIsLiquid(mat)) { info += " liquid"; ++i; }
                if (getIsCover(mat)) { info += " cover"; ++i; }
                if (i == 0) info += "-";
            }
        }

        RayTraceResult mop = null;
        BlockFace face = null;
        Entity ent = null;
        if (optShowBlock || optShowEntity) {
            mop = rayTrace(view, ZHandle.handle("getPlayerReach", getDefaultReach()), 1f);
            face = getBlockFace(mop);
            ent = getEntity(mop);
        }

        if (optShowBlock && face != null) {
            int idmeta = getIdMetaAt(getWorld(), face.x, face.y, face.z);
            Block block = getBlock(getState(idmeta));
            info += "\nTarget block: \u00a7b" + ZWrapper.getName(block)
                 +  "\u00a7f (\u00a7b" + getBlockId(idmeta)
                 +  "\u00a7f/\u00a7b" + getBlockMeta(idmeta)
                 +  "\u00a7f)";
        }

        if (optShowEntity && ent != null) {
            info += "\nTarget entity: \u00a7b" + ZWrapper.getName(ent)
                 +  "\u00a7f (" + getId(ent)
                 +  "\u00a7f)";
        }

        setMessage("info", info);
    }

    /*
    private static void respawnInfoMod() {
        infoDeathX = fix(posX);
        infoDeathY = fix(posY);
        infoDeathZ = fix(posZ);
    }
    */

    @Override
    protected void onGUIDraw(float delta) {
        if (optHideAchievement) killAchievement();
    }

    @Override
    protected String getTag() {
        String tag = "";
        Entity view = getView();
        int x = 0, y = 0, z = 0;
        if (view != null) {
            x = fix(getX(view)); y = fix(getY(view)); z = fix(getZ(view));
        }
        if (optTagPos && view != null) {
            tag += ""+x+","+y+","+z+" ";
        }
        if (optTagDirection && view != null) {
            Vec3d look = getLookVector(view, 1f);
            tag += getFineFacingName(getX(look), getY(look), getZ(look))+" ";
        }
        if (optTagCompass && view != null) {
            Vec3d look = getLookVector(view, 1f);
            Vec3i dir = getDirectionVec(EnumFacing.NORTH);
            tag += "("+getRelativeCompass(getX(dir), getZ(dir), getX(look), getZ(look))+") ";
        }
        if (optTagBiome && view != null) {
            tag += getBiomeName(x, z)+" ";
        }
        if (optTagFPS) {
            long time = System.currentTimeMillis();
            infoFrames++;
            if (time > infoTime + 1000) {
                infoFps = ""+infoFrames+"FPS ";
                infoTime = time;
                infoFrames = 0;
            }
            tag += infoFps;
        }
        if (optTagTime && !optShowTime) {
            tag += "[" + TimeHelper.getTime(ZHandle.handle("getSunOffset", getTime())) + "] ";
        }
        tag = tag.trim();
        return tag.length() == 0 ? null : tag;
    }

}
