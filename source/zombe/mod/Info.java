package zombe.mod;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import org.lwjgl.input.Keyboard;
import zombe.core.*;
import zombe.core.util.BlockFace;
import zombe.core.util.TimeHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.sun.tools.doclint.Entity.cap;
import static zombe.core.ZWrapper.*;

public final class Info extends ZMod {

    private int keyToggle;
    private boolean optHideAchievement, optTagPos, optTagDirection, optTagCompass, optTagBiome, optTagFPS, optTagTime, optShowPos, optShowDirection, optShowTime, optShowBiome, optShowItem, optShowBlock, optShowEntity;

    private boolean infoShow, infoDeath, infoServer;
    private int infoDeathX, infoDeathY, infoDeathZ, infoFrames = 0;
    private long infoTime = 0;
    @Nonnull private String infoFps = "";

    public Info() {
        super("info", "1.8", "9.0.2");

        this.addOption("optInfoHideAchievement", "Hide the obnoxious achievements", false);
        this.addOption("optInfoTagPos", "Tag your coordinates", false);
        this.addOption("optInfoTagCompass", "Tag a compass", true);
        this.addOption("optInfoTagDirection", "Tag your direction", false);
        this.addOption("optInfoTagBiome", "Tag the biome name", false);
        this.addOption("optInfoTagFPS", "Tag a FPS counter", true);
        this.addOption("optInfoTagTime", "Tag the time", false);
        this.addOption("keyInfoToggle", "Toggle info screen", Keyboard.KEY_F12);
        this.addOption("optInfoShowPos", "Show your coordinates", false);
        this.addOption("optInfoShowDirection", "Show your direction", false);
        this.addOption("optInfoShowBiome", "Show the biome name", false);
        this.addOption("optInfoShowTime", "Tag the time", false);
        this.addOption("optInfoShowItem", "Show selected item information", true);
        this.addOption("optInfoShowBlock", "Show target block information", true);
        this.addOption("optInfoShowEntity", "Show target entity information", true);
    }

    @Override
    protected void init() {
        this.infoDeath = false;
        this.infoShow = false;
        this.infoServer = false;
    }

    @Override
    protected void quit() {
        setMessage("info", null);
    }

    @Override
    protected void updateConfig() {
        this.keyToggle = getOptionKey("keyInfoToggle");
        this.optHideAchievement = getOptionBool("optInfoHideAchievement");
        this.optTagPos = getOptionBool("optInfoTagPos");
        this.optTagCompass = getOptionBool("optInfoTagCompass");
        this.optTagDirection = getOptionBool("optInfoTagDirection");
        this.optTagBiome = getOptionBool("optInfoTagBiome");
        this.optTagFPS = getOptionBool("optInfoTagFPS");
        this.optTagTime = getOptionBool("optInfoTagTime");
        this.optShowPos = getOptionBool("optInfoShowPos");
        this.optShowDirection = getOptionBool("optInfoShowDirection");
        this.optShowBiome = getOptionBool("optInfoShowBiome");
        this.optShowTime = getOptionBool("optInfoShowTime");
        this.optShowItem = getOptionBool("optInfoShowItem");
        this.optShowBlock = getOptionBool("optInfoShowBlock");
        this.optShowEntity = getOptionBool("optInfoShowEntity");
    }

    @Override
    protected void onWorldChange() {
        this.infoServer = false;
        this.infoDeath = false;
    }

    @SuppressWarnings({ "UnusedAssignment", "ConstantConditions" })
    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        if (!isInMenu() && wasKeyPressedThisTick(this.keyToggle)) {
            this.infoShow = !this.infoShow;
        }

        setMessage("info", null);
        String info = "";
        Entity view = getView();
        if (view == null) {
            view = player;
        }

        double px = getX(player), py = getY(player), pz = getZ(player);
        double vx = getX(view), vy = getY(view), vz = getZ(view);
        int x = fix(vx), y = fix(vy), z = fix(vz);

        if (getHealth(player) <= 0) {
            this.infoDeath = true;
            this.infoDeathX = fix(px);
            this.infoDeathY = fix(py);
            this.infoDeathZ = fix(pz);
        }

        if (isInMenu() && !(getMenu() instanceof GuiChat) || !this.infoShow) {
            return;
        }

        int mx, my, mz, id, meta, cap, cnt, cx = x >> 4, cz = z >> 4;
        final long timeRT = getTime();
        final long time = ZHandle.handle("getSunOffset", timeRT);
        final BlockPos at;

        // fog & exp
        info += "\nFog: \u00a7b" + getViewDistance() + "\u00a7f    Exp-orbs: \u00a7b" + player.experienceTotal;

        // light level
        if (y >= 0) {
            info += "\nLight real=\u00a7b" + getRealLightLevel(x, y, z) + "\u00a7f (block=\u00a78" + getBlockLightLevel(x, y, z) + "\u00a7f sky=\u00a7e" + getSkyLightLevel(x, y, z) + "\u00a7f)";
        }

        // chunk
        info += "\nChunk: \u00a7b" + cx + "\u00a7f,\u00a7b" + cz;

        // your location
        if (this.optShowPos && !this.optTagPos) {
            info += "  Position: \u00a7b" + x + "\u00a7f, \u00a7b" + y + "\u00a7f, \u00a7b" + z;
        }

        // biome
        if (this.optShowBiome && !this.optTagBiome) {
            info += "  Biome: \u00a7b" + getBiomeName(x, z);
        }
        if (this.infoServer) {
            // slimes
            Random rnd = new Random(getSeed() + (long) (cx * cx * 0x4c1906) + (long) (cx * 0x5ac0db) + (long) (cz * cz) * 0x4307a7L + (long) (cz * 0x5f24f) ^ 0x3ad8025f); // the silliest nonsense i have ever seen x_x
            info += "  Slimes: \u00a7b" + (rnd.nextInt(10) == 0 ? "yes" : "no ");

            // stronghold
            // TODO update MCP func_190528_a to findClosestStructure
            //BlockPos stronghold = getWorld().findClosestStructure("Stronghold", x, y, z);
            BlockPos stronghold = getWorld().func_190528_a("Stronghold", new BlockPos(x, y, z), true);
            if (stronghold != null) {
                info += "\nStronghold: \u00a7b" + (mx = getX(stronghold)) + "\u00a7f , \u00a7b" + getY(stronghold) + "\u00a7f , \u00a7b" + (mz = getZ(stronghold));
                mx -= x;
                mz -= z;
                info += "\u00a7f (\u00a7b" + (int) Math.sqrt(mx * mx + mz * mz) + "\u00a7fm)";
            }

            // spawn
            info += "\nSpawn: \u00a7b" + (mx = getSpawnX()) + "\u00a7f , \u00a7b" + (my = getSpawnY()) + "\u00a7f , \u00a7b" + (mz = getSpawnZ());
            mx -= x;
            mz -= z;
            info += "\u00a7f (\u00a7b" + (int) Math.sqrt(mx * mx + mz * mz) + "\u00a7fm)";
        }

        // player bed
        at = getBed(player);
        if (at != null) {
            info += "\nYour bed: \u00a7b" + (mx = getX(at)) + "\u00a7f,\u00a7b" + (my = getY(at)) + "\u00a7f,\u00a7b" + (mz = getZ(at));
            mx -= x;
            mz -= z;
            info += "\u00a7f (\u00a7b" + (int) Math.sqrt(mx * mx + mz * mz) + "\u00a7fm)";
        }

        // last death
        if (this.infoDeath) {
            info += "\nYou died:   \u00a7b" + (mx = this.infoDeathX) + "\u00a7f , \u00a7b" + (my = this.infoDeathY) + "\u00a7f , \u00a7b" + (mz = this.infoDeathZ);
            mx -= x;
            mz -= z;
            info += "\u00a7f (\u00a7b" + (int) Math.sqrt(mx * mx + mz * mz) + "\u00a7fm)";
        }

        // world name
        if (this.infoServer) {
            info += "\nWorld:  name=\u00a7b" + getWorldName() + "  seed=\u00a7b" + getSeed();

            info += "\nRaining: \u00a7b" + (getRaining() ? "yes" : "no");
            if (!getIsHell()) {
                info += "\u00a7f the next \u00a7b" + (getRainingTime() / 20) + "\u00a7fs";
            }
            info += "\nThunder: \u00a7b" + (getThunder() ? "yes" : "no");
            if (!getIsHell()) {
                info += "\u00a7f the next \u00a7b" + (getThunderTime() / 20) + "\u00a7fs";
            }
        }

        // time
        info += "\nWorld Age (real time): \u00a7b" + TimeHelper.getRealTime(timeRT);
        if (this.optShowTime) {
            info += "\nTime: \u00a7b" + TimeHelper.getTime(time);
            if (time != timeRT) {
                info += "\u00a7f (actual time: \u00a7b" + TimeHelper.getTime(timeRT) + "\u00a7f)";
            }
        }

        // item in hand
        ItemStack stack = getStacks(player)[getCurrentSlot(player)];
        if (this.optShowItem && stack != null) {
            id = getId(stack);
            meta = getMeta(stack);
            cnt = getStackSize(stack);
            Item item = getItem(id);
            info += "\nSelected item: \u00a7b" + ZWrapper.getName(item) + "\u00a7f (\u00a7b" + id + "\u00a7f/" + (hasSubTypes(item) ? "\u00a7b" + meta : "(\u00a7b" + meta + "\u00a7f)") + "\u00a7f)  stack: \u00a7b" + cnt + "\u00a7f/\u00a7b" + getItemMax(item);
            Block block = getBlock(item);

            info += "\nType: \u00a7b" + (block != null ? "Block" : "Item");
            if ((cap = getItemDmgCap(item)) != 0) {
                info += "\u00a7f  damage: \u00a7b" + meta + "\u00a7f/\u00a7b" + cap;
            }
            if (block != null) {
                info += "\u00a7f  id: \u00a7b" + ZWrapper.getName(block) + "\u00a7f (\u00a7b" + getId(block) + "\u00a7f?\u00a7b" + getBlockMeta(stack) + "\u00a7f)";
                info += "\nProperties:   hardness=\u00a7b" + getBlockHardness(block) + "\u00a7f  resistance=\u00a7b" + getBlockResist(block) + "\u00a7f  slip=\u00a7b" + getBlockSlip(block);
                info += "\nLight:   emission=\u00a7b" + getBlockLight(id) + "\u00a7f  opacity=\u00a7b" + (getBlockIsOpaque(id) ? "opaque" : getBlockOpacity(id));
                info += "\nFire:   spread=\u00a7b" + getFireSpread(id) + "\u00a7f  burn=\u00a7b" + getFireBurn(id);
                Material mat = getBlockMaterial(block);
                info += "\nMaterial:\u00a7b";
                int i = 0;
                if (getIsSolid(mat)) {
                    info += " solid";
                    ++i;
                }
                if (getIsBurnable(mat)) {
                    info += " burnable";
                    ++i;
                }
                if (getIsReplaceable(mat)) {
                    info += " replaceable";
                    ++i;
                }
                if (getIsLiquid(mat)) {
                    info += " liquid";
                    ++i;
                }
                if (getIsCover(mat)) {
                    info += " cover";
                    ++i;
                }
                if (i == 0) {
                    info += "-";
                }
            }
        }

        RayTraceResult mop = null;
        BlockFace face = null;
        Entity ent = null;
        if (this.optShowBlock || this.optShowEntity) {
            mop = rayTrace(view, ZHandle.handle("getPlayerReach", getDefaultReach()), 1f);
            face = getBlockFace(mop);
            ent = getEntity(mop);
        }

        if (this.optShowBlock && face != null) {
            int idmeta = getIdMetaAt(getWorld(), face.x, face.y, face.z);
            Block block = getBlock(getState(idmeta));
            info += "\nTarget block: \u00a7b" + ZWrapper.getName(block) + "\u00a7f (\u00a7b" + getBlockId(idmeta) + "\u00a7f/\u00a7b" + getBlockMeta(idmeta) + "\u00a7f)";
        }

        if (this.optShowEntity && ent != null) {
            info += "\nTarget entity: \u00a7b" + ZWrapper.getName(ent) + "\u00a7f (" + getId(ent) + "\u00a7f)";
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
        if (this.optHideAchievement) {
            killAchievement();
        }
    }

    @Nullable
    @Override
    protected String getTag() {
        String tag = "";
        Entity view = getView();
        int x = 0, y = 0, z = 0;
        if (view != null) {
            x = fix(getX(view));
            y = fix(getY(view));
            z = fix(getZ(view));
        }
        if (this.optTagPos && view != null) {
            tag += "" + x + "," + y + "," + z + " ";
        }
        if (this.optTagDirection && view != null) {
            Vec3d look = getLookVector(view, 1f);
            tag += getFineFacingName(getX(look), getY(look), getZ(look)) + " ";
        }
        if (this.optTagCompass && view != null) {
            Vec3d look = getLookVector(view, 1f);
            Vec3i dir = getDirectionVec(EnumFacing.NORTH);
            tag += "(" + getRelativeCompass(getX(dir), getZ(dir), getX(look), getZ(look)) + ") ";
        }
        if (this.optTagBiome && view != null) {
            tag += getBiomeName(x, z) + " ";
        }
        if (this.optTagFPS) {
            long time = System.currentTimeMillis();
            this.infoFrames++;
            if (time > this.infoTime + 1000) {
                this.infoFps = "" + this.infoFrames + "FPS ";
                this.infoTime = time;
                this.infoFrames = 0;
            }
            tag += this.infoFps;
        }
        if (this.optTagTime && !this.optShowTime) {
            tag += "[" + TimeHelper.getTime(ZHandle.handle("getSunOffset", getTime())) + "] ";
        }
        tag = tag.trim();
        return tag.isEmpty() ? null : tag;
    }

}
