package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import zombe.core.*;
import zombe.core.util.Color;
import zombe.core.util.GuiHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static zombe.core.ZWrapper.*;

public final class Cheat extends ZMod {

    private static final int MOBS_MAX = ZWrapper.MAXTYPE;
    private static final int ORES_MAX = 4096;
    private static final int ITEMS_MAX = 400;
    private static final int MARKS_MAX = 16384;

    @Nonnull ArrayList<Color> cheatMobs = new ArrayList<>();
    @Nonnull ArrayList<Color> cheatOres = new ArrayList<>();
    @Nonnull ArrayList<Color> cheatType = new ArrayList<>();
    @Nonnull ArrayList<Vec3d> cheatMark = new ArrayList<>();
    @Nonnull private ArrayList<Boolean> cheatCarryBlocks = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Nonnull private ArrayList<Boolean> cheatDamage = new ArrayList<>();
    @SuppressWarnings("ConstantConditions")
    @Nonnull private Field fCarryBlocks = getField(EntityEnderman.class, "ee_canCarryBlocks");

    private String tagCheat, tagMobs, tagOres;
    private int keyCheat, keyShowMobs, keyShowOres, keySee, keyHighlight, keyRemoveFire, keyHealth, keyDamage;
    private int optHighlightMode, optShowOresRangeH, optShowOresRangeV;
    private int cheatCur = 0, cheatUpdate;
    private int cheatArrowCount;
    private float optSeeDist, optShowMobsRange;
    private float cheatGamma;
    private boolean modCheatAllowed = true;
    private boolean optCheat, optFallDamage, optDisableDamage, optRestoreHealth, optShowDangerous, optShowNeutral, optSeeIsToggle, optShowMobsSize;
    private boolean optInfArrows, optInfArmor, optInfSword, optInfTools, optFireImmune, optShowHealth;
    private boolean optNoAir, optNerfEnderman;
    private boolean optCheatInfArrows;

    private boolean cheating = false, cheatShowMobs = false, cheatShowOres = false, cheatSee, cheatHighlight, cheatCarryOverride;

    public Cheat() {
        super("cheat", "1.8", "9.0.0");
        this.registerHandler("isCheating");
        this.registerListener("afterPlayerMove");
        this.registerListener("onSortAndRender");
        this.registerListener("onServerUpdate");

        this.addOption("keyCheat", "Cheat mode toggle key", Keyboard.KEY_Y);
        this.addOption("optCheat", "Cheat mode activated by default", false);
        this.addOption("tagCheater", "Tag shown when cheats activated", "cheater");
        this.addOption("singleplayer and multiplayer features");
        this.addOption("optCheatFallDamage", "Allow fall damage", true);
        this.addOption("keyCheatShowMobs", "Show monsters toggle key", Keyboard.KEY_M);
        this.addOption("tagCheatShowMobs", "Tag shown when showing mobs", "mobs");
        this.addOption("optCheatShowMobs", "Monsters and colors to show", "Other/0x888888, Player/0x0000ff, Spider/0xff6666, Cavespider/0xff8866, Skelly/0xffffff, Creeper/0x66ff66, Zombie/0xffff66, Slimes/0xff66ff, Silverfish/0x66ffff, Ghast/0xffffff, LavaSlime/0xff6666, Blaze/0xffff66, Pigzombie/0x882200, Wolf/0x880088, Enderman/0x008800, Squid/0x000088, Cow/0x554400, Sheep/0x888888, Pig/0x884400, Chicken/0x999966, MushroomCow/0x880000, Villager/0x008888, SnowMan/0x666688, Golem/0x668888, Ocelot/888866");
        this.addOption("optCheatShowMobsRange", "Range at which to show mobs", 1f, 1f, 256f, true);
        this.addOption("optCheatShowMobsSize", "Adjust markings to mobs size", false);
        this.addOption("keyCheatShowOres", "Show ores toggle key", Keyboard.KEY_O);
        this.addOption("tagCheatShowOres", "Tag shown when showing ores", "ores");
        this.addOption("optCheatShowOres", "Ores and colors to show", "oreI/0xff6600, clay/Cyan, oreG/0xffee00, oreD/LightCyan, mossy/LightGreen, oreL/LightBlue, oreR/LightRed, cage/LightMagenta, oreC/DarkWhite, smoothSF/DarkRed, 129/Green");
        this.addOption("optCheatShowOresRangeH", "Horizontal range to show ores", 16, 4, 128);
        this.addOption("optCheatShowOresRangeV", "Vertical range to show ores", 64, 4, 128);
        this.addOption("keyCheatHighlight", "Highlighting mode toggle key", Keyboard.KEY_H);
        this.addOption("keyCheatSee", "See through nearby objects key", Keyboard.KEY_I);
        this.addOption("optCheatSeeIsToggle", "See Through key is a toggle", false);
        this.addOption("optCheatSeeDist", "See Through distance in meters", 4f, 1f, 32f, true);
        this.addOption("singleplayer-only features");
        this.addOption("optCheatNerfEnderman", "Allow endermen to pick blocks", true);
        this.addOption("keyCheatHealth", "Toggle health regeneration", Keyboard.KEY_NONE);
        this.addOption("optCheatRestoreHealth", "Regenerate health by default", false);
        this.addOption("keyCheatDamage", "Toggle damages", Keyboard.KEY_NONE);
        this.addOption("optCheatDisableDamage", "Disable damages by default", false);
        this.addOption("optCheatInfArrows", "Enable infinite arrows", false);
        this.addOption("optCheatInfArmor", "Enable infinite armor durability", false);
        this.addOption("optCheatInfSword", "Enable infinite sword durability", false);
        this.addOption("optCheatInfTools", "Enable infinite tools durability", false);
        this.addOption("optCheatFireImmune", "Enable fire immunity", false);
        this.addOption("optCheatNoAir", "Disable need for air", false);
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        switch (name) {
            case "isCheating":
                return this.isCheating();
            case "onServerUpdate":
                this.onServerUpdate((EntityPlayerMP) arg);
                break;
            case "afterPlayerMove":
                this.afterPlayerMove();
                break;
            case "onSortAndRender":
                this.startCheatRender();
                break;
        }

        return arg;
    }

    @Override
    protected void init() {
        this.cheatMobs.ensureCapacity(MOBS_MAX);
        this.cheatOres.ensureCapacity(ORES_MAX);
        this.cheatDamage.ensureCapacity(ITEMS_MAX);
        this.cheatCur = 0;
        this.cheatUpdate = 0;
    }

    @Override
    protected void quit() {
        this.cheatMobs.clear();
        this.cheatOres.clear();
        this.cheatType.clear();
        this.cheatMark.clear();
        this.cheatDamage.clear();
    }

    @Override
    protected void updateConfig() {
        this.tagCheat = getOptionString("tagCheater");
        this.tagMobs = getOptionString("tagCheatShowMobs");
        this.tagOres = getOptionString("tagCheatShowOres");
        this.optShowHealth = false;
        //optCheatShowHealth = getSetBool(optCheatShowHealth, "optCheatShowHealth", true, "Show critter health");
        this.keyRemoveFire = Keyboard.KEY_NONE;
        //keyCheatRemoveFire = getSetBind(keyCheatRemoveFire, "keyCheatRemoveFire",    Keyboard.KEY_N, "Remove fire nearby");

        this.keyCheat = getOptionKey("keyCheat");
        this.keyHighlight = getOptionKey("keyCheatHighlight");
        this.keyHealth = getOptionKey("keyCheatHealth");
        this.keyDamage = getOptionKey("keyCheatDamage");
        this.keyShowMobs = getOptionKey("keyCheatShowMobs");
        this.keyShowOres = getOptionKey("keyCheatShowOres");
        this.keySee = getOptionKey("keyCheatSee");
        this.optShowMobsRange = getOptionFloat("optCheatShowMobsRange");
        this.optShowOresRangeH = getOptionInt("optCheatShowOresRangeH");
        this.optShowOresRangeV = getOptionInt("optCheatShowOresRangeV");
        this.optSeeDist = getOptionFloat("optCheatSeeDist");
        this.optCheat = getOptionBool("optCheat");
        this.optSeeIsToggle = getOptionBool("optCheatSeeIsToggle");
        this.optShowMobsSize = getOptionBool("optCheatShowMobsSize");
        this.optRestoreHealth = getOptionBool("optCheatRestoreHealth");
        this.optDisableDamage = getOptionBool("optCheatDisableDamage");
        this.optFallDamage = getOptionBool("optCheatFallDamage");
        this.optFireImmune = getOptionBool("optCheatFireImmune");
        this.optNerfEnderman = getOptionBool("optCheatNerfEnderman");
        this.optNoAir = getOptionBool("optCheatNoAir");

        this.optCheatInfArrows = getOptionBool("optCheatInfArrows");

        this.optInfArmor = false;
        //optCheatInfArmor = getSetBool(optCheatInfArmor, "optCheatInfArmor", false, "Indestructible armor");
        //for (int i=298;i<=317;i++) cheatDamage[i] = optInfArmor;
        this.optInfSword = false;
        //optCheatInfSword = getSetBool(optCheatInfSword, "optCheatInfSword", false, "Indestructible sword/bow");
        //cheatDamage[267] = cheatDamage[268] = cheatDamage[272] = cheatDamage[276] = cheatDamage[283] = cheatDamage[261] = optInfSword;
        this.optInfTools = false;
        //optCheatInfTools = getSetBool(optCheatInfTools, "optCheatInfTools", false, "Indestructible tools");
        /*cheatDamage[256] = cheatDamage[257] = cheatDamage[258] = cheatDamage[259]
                         = cheatDamage[269] = cheatDamage[270] = cheatDamage[271]
                         = cheatDamage[273] = cheatDamage[274] = cheatDamage[275]
                         = cheatDamage[277] = cheatDamage[278] = cheatDamage[279]
                         = cheatDamage[284] = cheatDamage[285] = cheatDamage[286]
                         = cheatDamage[290] = cheatDamage[291] = cheatDamage[292]
                         = cheatDamage[293] = cheatDamage[294] = cheatDamage[346]
                         = cheatDamage[359] = optInfTools;
*/
        this.cheatMobs.clear();
        this.cheatOres.clear();

        Map<Integer, Color> colormap = parseEntityColorMap(getOptionString("optCheatShowMobs"));
        for (Map.Entry<Integer, Color> entry : colormap.entrySet()) {
            int id = entry.getKey();
            if (0 <= id && id < MOBS_MAX) {
                this.cheatMobs.set(id, entry.getValue());
            }
        }

        colormap = parseBlockColorMap(getOptionString("optCheatShowOres"));
        for (Map.Entry<Integer, Color> entry : colormap.entrySet()) {
            int id = entry.getKey();
            if (0 <= id && id < ORES_MAX) {
                this.cheatOres.set(id, entry.getValue());
            }
        }
    }

    @Override
    protected void onWorldChange() {
        this.cheating = this.optCheat && this.modCheatAllowed;
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        this.modCheatAllowed = ZHandle.handle("allowCheats", true);

        if (getGamma() < 100f) {
            this.cheatGamma = getGamma();
        }
        setGamma((this.cheating && this.cheatHighlight && (!isInMenu() || getMenu() instanceof GuiChat || getMenu() instanceof GuiContainer)) ? 1000f : this.cheatGamma);

        boolean enable = this.optNerfEnderman && !isMultiplayer();
        if (enable != this.cheatCarryOverride) {
            try {
                this.cheatCarryOverride = enable;
                Object arr = getValue(this.fCarryBlocks, null);
                if (this.cheatCarryBlocks.isEmpty()) {
                    this.cheatCarryBlocks.ensureCapacity(256);
                    for (int i = 0; i < 256; i++) {
                        this.cheatCarryBlocks.set(i, Array.getBoolean(arr, i));
                    }
                }
                for (int i = 0; i < 256; i++) {
                    Array.setBoolean(arr, i, !enable && this.cheatCarryBlocks.get(i));
                }
            } catch (Exception e) {
            }
        }

        if (!isInMenu() && wasKeyPressedThisTick(this.keyCheat)) {
            this.cheating = !this.cheating;
            if (!this.modCheatAllowed && this.cheating) {
                this.cheating = false;
                chatClient("\u00a74zombe's \u00a72cheat\u00a74-mod is not allowed on this server.");
            }
        }

        if (!this.cheating) {
            return;
        }
        if (!isInMenu()) {
            if (wasKeyPressedThisTick(this.keyShowMobs)) {
                this.cheatShowMobs = !this.cheatShowMobs;
            }
            if (wasKeyPressedThisTick(this.keyShowOres)) {
                this.cheatShowOres = !this.cheatShowOres;
            }
            if (wasKeyPressedThisTick(this.keyHighlight)) {
                this.cheatHighlight = !this.cheatHighlight;
            }
            if (wasKeyPressedThisTick(this.keyHealth)) {
                this.optRestoreHealth = !this.optRestoreHealth;
            }
            if (wasKeyPressedThisTick(this.keyDamage)) {
                this.optDisableDamage = !this.optDisableDamage;
            }
            if (this.optSeeIsToggle) {
                if (wasKeyPressedThisTick(this.keySee)) {
                    this.cheatSee = !this.cheatSee;
                }
            } else {
                this.cheatSee = isKeyDownThisTick(this.keySee);
            }
            if (!isMultiplayer() && wasKeyPressedThisTick(this.keyRemoveFire)) {
                Entity view = getView();
                assert view != null;

                BlockPos pos = getPos(view);
                int x = getX(pos), y = getY(pos), z = getZ(pos);
                World world = getWorld();
                for (int dx = -16; dx <= 16; ++dx) {
                    for (int dy = -16; dy <= 16; ++dy) {
                        for (int dz = -16; dz <= 16; ++dz) {
                            if (getIdAt(world, x + dx, y + dy, z + dz) == 51) {
                                setIdAt(world, 0, MARK_BOTH, x + dx, y + dy, z + dz);
                            }
                        }
                    }
                }
            }
        }

        // TODO why is this commented out and what did it do?

            /*
        if (!isMultiplayer()) {
            if (!optCheatFallDamage) setFall(player, 0f);
            boolean arrowChk = true;
            if (optCheatInfArrows || optCheatInfArmor || optCheatInfSword || optCheatInfTools)
            for (int slot=0;slot<invItemsArr.length;slot++)
            if (invItemsArr[slot] != null) {
                ItemStack items = invItemsArr[slot];
                int id = getItemsId(items);
                if (id < 256 || id >= cheatItems) continue;
                if (optCheatInfArrows && id==262 && arrowChk) {
                    arrowChk = false;
                    int count = getItemsCount(items);
                    if (cheatArrowCount - 1 == count) setItemsCount(items, ++count);
                    cheatArrowCount = count;
                } else if (cheatDamage[id]) setItemsInfo(items, 0);
            }
            if (optCheatInfArmor) for (int slot=0;slot<invArmorsArr.length;slot++) if (invArmorsArr[slot] != null) {
                int id = getItemsId(invArmorsArr[slot]);
                if (id < 256 || id >= cheatItems) continue;
                if (cheatDamage[id]) setItemsInfo(invArmorsArr[slot], 0);
            }
        }
            */
    }

    @Override
    protected void onWorldDraw(float delta, float px, float py, float pz) {
        if (!this.cheatShowMobs && !this.cheatShowOres && !this.optShowHealth) {
            return;
        }

        List list = getEntities();
        Entity view = getView();

        double mx, my, mz, dx, dy, dz;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (this.cheating && this.optShowHealth && !isMultiplayer()) {
            GL11.glColor3ub((byte) 0, (byte) 128, (byte) 0);
            GL11.glBegin(GL11.GL_QUADS);
            for (Object obj : list) {
                if (!(obj instanceof EntityLiving) || obj == view) {
                    continue;
                }

                EntityLiving ent = (EntityLiving) obj;
                Vec3d pos = getPositionDelta(ent, delta);
                float health = getHealth(ent);
                mx = getX(pos);
                my = getY(pos) + getYFix(ent) + getHeight(ent);
                mz = getZ(pos);
                dx = mz - pz;
                dz = -(mx - px);
                double d = Math.sqrt(dx * dx + dz * dz);
                double w = 0.25 * health;
                mx -= px;
                my -= py;
                mz -= pz;
                if (d < 0.1 || d > 64) {
                    continue;
                }

                dx /= d;
                dz /= d;
                while (health > 0) {
                    double ax1, ax2, az1, az2;
                    w -= 0.3;
                    ax1 = w * dx * 0.1;
                    az1 = w * dz * 0.1;
                    w -= 0.7;
                    ax2 = w * dx * 0.1;
                    az2 = w * dz * 0.1;
                    GL11.glVertex3d(mx + ax1, my - 0.1, mz + az1);
                    GL11.glVertex3d(mx + ax2, my - 0.1, mz + az2);
                    GL11.glVertex3d(mx + ax2, my + (health == 1 ? 0 : 0.1), mz + az2);
                    GL11.glVertex3d(mx + ax1, my + (health == 1 ? 0 : 0.1), mz + az1);
                    health -= 2;
                }
            }
            GL11.glEnd();
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_FOG);
        if (this.modCheatAllowed && this.cheating && this.cheatShowMobs) {
            float range = this.optShowMobsRange * this.optShowMobsRange;
            //GL11.glEnable(GL11.GL_LINE_STIPPLE);
            //GL11.glLineStipple(1, (short)0x5555);
            GL11.glBegin(GL11.GL_LINES);
            for (Object obj : list) {
                Entity ent = (Entity) obj;
                int id = getId(ent);
                Color col = (0 <= id && id < MOBS_MAX) ? this.cheatMobs.get(id) : null;
                if (col == null || obj == view) {
                    continue;
                }

                Vec3d pos = getPositionDelta(ent, delta);
                mx = getX(pos);
                my = getY(pos) + getYFix(ent);
                mz = getZ(pos);
                dx = mx - px;
                dy = my - py;
                dz = mz - pz;
                if (this.optShowMobsRange > 0 && dx * dx + dy * dy + dz * dz > range) {
                    continue;
                }

                float height = (this.optShowMobsSize || !(ent instanceof EntityLiving)) ? getHeight(ent) : 2.0f;
                GL11.glColor3ub(col.rb, col.gb, col.bb);
                GL11.glVertex3d(dx, dy, dz);
                GL11.glVertex3d(dx, dy + height, dz);
            }
            GL11.glEnd();
            //GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }
        GL11.glBegin(GL11.GL_LINES);
        if (this.modCheatAllowed && this.cheating && this.cheatShowOres) {
            if (--this.cheatUpdate < 0) {
                this.cheatUpdate = 17;
                this.cheatReCheck(fix(px), fix(py), fix(pz));
            }
            for (int i = 0; i < this.cheatCur; ++i) {
                Color col = this.cheatType.get(i);
                Vec3d pos = this.cheatMark.get(i);
                if (col == null || pos == null) {
                    continue;
                }

                GL11.glColor3ub(col.rb, col.gb, col.bb);
                mx = getX(pos) - px;
                my = getY(pos) - py;
                mz = getZ(pos) - pz;
                GL11.glVertex3d(mx + 0.25, my + 0.25, mz + 0.25);
                GL11.glVertex3d(mx - 0.25, my - 0.25, mz - 0.25);
                GL11.glVertex3d(mx + 0.25, my + 0.25, mz - 0.25);
                GL11.glVertex3d(mx - 0.25, my - 0.25, mz + 0.25);
                GL11.glVertex3d(mx + 0.25, my - 0.25, mz + 0.25);
                GL11.glVertex3d(mx - 0.25, my + 0.25, mz - 0.25);
                GL11.glVertex3d(mx + 0.25, my - 0.25, mz - 0.25);
                GL11.glVertex3d(mx - 0.25, my + 0.25, mz + 0.25);
            }
        }
        GL11.glEnd();
    }

    @Nullable
    @Override
    protected String getTag() {
        if (!this.modCheatAllowed || !this.cheating) {
            return null;
        }

        String tag = this.tagCheat;
        if (this.cheatShowMobs) {
            tag += ' ' + this.tagMobs;
        }
        if (this.cheatShowOres) {
            tag += ' ' + this.tagOres;
        }
        return tag;
    }

    private void cheatReCheck(int pX, int pY, int pZ) {
        this.cheatCur = 0;
        World world = getWorld();
        for (int x = pX - this.optShowOresRangeH; x < pX + this.optShowOresRangeH; ++x) {
            for (int y = pY - this.optShowOresRangeV; y < pY + this.optShowOresRangeV; ++y) {
                for (int z = pZ - this.optShowOresRangeH; z < pZ + this.optShowOresRangeH; ++z) {
                    int id = getIdAt(world, x, y, z);
                    if (id < 0 || id > this.cheatOres.size()) {
                        continue;
                    }
                    Color color = this.cheatOres.get(id);
                    if (color == null) {
                        continue;
                    }
                    this.cheatType.set(this.cheatCur, color);
                    this.cheatMark.set(this.cheatCur, new Vec3d(x + 0.5f, y + 0.5f, z + 0.5f));
                    ++this.cheatCur;
                    if (this.cheatCur >= this.cheatMark.size()) {
                        return;
                    }
                }
            }
        }
    }

    private void onServerUpdate(@Nonnull EntityPlayerMP ent) {
        if (!this.modCheatAllowed) {
            return;
        }
        ent.capabilities.disableDamage = this.cheating && this.optDisableDamage || ent.capabilities.isCreativeMode;
        setFireImmune(ent, this.cheating && this.optFireImmune);
        if (this.cheating && this.optRestoreHealth && getHealth(ent) < getMaxHealth(ent)) {
            setHealth(ent, getHealth(ent) + 1);
        }
        if (this.cheating && this.optNoAir) {
            setAir(ent, 300);
        }
        if (this.cheating && !this.optFallDamage) {
            setFall(ent, 0);
        }
    }

    private void afterPlayerMove() {
        if (this.cheating && !this.optFallDamage) {
            setFall(getPlayer(), 0f);
            setOnGround(getPlayer(), true);
        }
    }

    private boolean isCheating() {
        return this.cheating;
    }

    private void startCheatRender() {
        try {
            if (this.modCheatAllowed && this.cheating && this.cheatSee) {
                GuiHelper.setObliqueNearPlaneClip(0.0f, 0.0f, -1.0f, -this.optSeeDist);
            }
        } catch (Exception error) {
            showOnscreenError("Cheat: see-through setup failed", error);
        }
    }
}
