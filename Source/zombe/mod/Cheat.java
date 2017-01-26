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
import zombe.core.ZHandle;
import zombe.core.ZMod;
import zombe.core.ZWrapper;
import zombe.core.util.Color;
import zombe.core.util.GuiHelper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static zombe.core.ZWrapper.*;

public final class Cheat extends ZMod {

    private static boolean modCheatAllowed = true;
    private static String tagCheat, tagMobs, tagOres;
    private static int keyCheat, keyShowMobs, keyShowOres, keySee, keyHighlight, keyRemoveFire, keyHealth, keyDamage;
    private static boolean optCheat, optFallDamage, optDisableDamage, optRestoreHealth, optShowDangerous, optShowNeutral, optSeeIsToggle, optShowMobsSize;
    private static boolean optInfArrows, optInfArmor, optInfSword, optInfTools, optFireImmune, optShowHealth;
    private static boolean optNoAir, optNerfEnderman;
    private static int optHighlightMode, optShowOresRangeH, optShowOresRangeV;
    private static float optSeeDist, optShowMobsRange;

    private static boolean optCheatInfArrows;
    private static boolean cheating = false, cheatShowMobs = false, cheatShowOres = false, cheatSee, cheatDamage[], cheatHighlight;
    private static int cheatCur = 0, cheatUpdate;
    private static float cheatGamma;
    private static boolean cheatCarryBlocks[], cheatCarryOverride;
    private static Field fCarryBlocks = getField(EntityEnderman.class, "ee_canCarryBlocks");
    private static Color cheatMobs[], cheatOres[], cheatType[];
    private static Vec3d cheatMark[];
    private static int cheatArrowCount;
    private static final int MOBS_MAX = ZWrapper.MAXTYPE, ORES_MAX = 4096,
        ITEMS_MAX = 400, MARKS_MAX = 16384;

    public Cheat() {
        super("cheat", "1.8", "9.0.0");
        registerHandler("isCheating");
        registerListener("afterPlayerMove");
        registerListener("onSortAndRender");
        registerListener("onServerUpdate");

        addOption("keyCheat", "Cheat mode toggle key", Keyboard.KEY_Y);
        addOption("optCheat", "Cheat mode activated by default", false);
        addOption("tagCheater", "Tag shown when cheats activated", "cheater");
        addOption("singleplayer and multiplayer features");
        addOption("optCheatFallDamage", "Allow fall damage", true);
        addOption("keyCheatShowMobs", "Show monsters toggle key", Keyboard.KEY_M);
        addOption("tagCheatShowMobs", "Tag shown when showing mobs", "mobs");
        addOption("optCheatShowMobs", "Monsters and colors to show", "Other/0x888888, Player/0x0000ff, Spider/0xff6666, Cavespider/0xff8866, Skelly/0xffffff, Creeper/0x66ff66, Zombie/0xffff66, Slimes/0xff66ff, Silverfish/0x66ffff, Ghast/0xffffff, LavaSlime/0xff6666, Blaze/0xffff66, Pigzombie/0x882200, Wolf/0x880088, Enderman/0x008800, Squid/0x000088, Cow/0x554400, Sheep/0x888888, Pig/0x884400, Chicken/0x999966, MushroomCow/0x880000, Villager/0x008888, SnowMan/0x666688, Golem/0x668888, Ocelot/888866");
        addOption("optCheatShowMobsRange", "Range at which to show mobs", 1f, 1f, 256f, true);
        addOption("optCheatShowMobsSize", "Adjust markings to mobs size", false);
        addOption("keyCheatShowOres", "Show ores toggle key", Keyboard.KEY_O);
        addOption("tagCheatShowOres", "Tag shown when showing ores", "ores");
        addOption("optCheatShowOres", "Ores and colors to show", "oreI/0xff6600, clay/Cyan, oreG/0xffee00, oreD/LightCyan, mossy/LightGreen, oreL/LightBlue, oreR/LightRed, cage/LightMagenta, oreC/DarkWhite, smoothSF/DarkRed, 129/Green");
        addOption("optCheatShowOresRangeH", "Horizontal range to show ores", 16, 4, 128);
        addOption("optCheatShowOresRangeV", "Vertical range to show ores", 64, 4, 128);
        addOption("keyCheatHighlight", "Highlighting mode toggle key", Keyboard.KEY_H);
        addOption("keyCheatSee", "See through nearby objects key", Keyboard.KEY_I);
        addOption("optCheatSeeIsToggle", "See Through key is a toggle", false);
        addOption("optCheatSeeDist", "See Through distance in meters", 4f, 1f, 32f, true);
        addOption("singleplayer-only features");
        addOption("optCheatNerfEnderman", "Allow endermen to pick blocks", true);
        addOption("keyCheatHealth", "Toggle health regeneration", Keyboard.KEY_NONE);
        addOption("optCheatRestoreHealth", "Regenerate health by default", false);
        addOption("keyCheatDamage", "Toggle damages", Keyboard.KEY_NONE);
        addOption("optCheatDisableDamage", "Disable damages by default", false);
        addOption("optCheatInfArrows", "Enable infinite arrows", false);
        addOption("optCheatInfArmor", "Enable infinite armor durability", false);
        addOption("optCheatInfSword", "Enable infinite sword durability", false);
        addOption("optCheatInfTools", "Enable infinite tools durability", false);
        addOption("optCheatFireImmune", "Enable fire immunity", false);
        addOption("optCheatNoAir", "Disable need for air", false);
    }

    @Override
    protected void init() {
        if (cheatMobs == null)
        cheatMobs = new Color[MOBS_MAX];
        if (cheatOres == null)
        cheatOres = new Color[ORES_MAX];
        cheatType = new Color[MARKS_MAX];
        cheatMark = new Vec3d[MARKS_MAX];
        cheatCur = 0;
        cheatUpdate = 0;
        cheatDamage = new boolean[ITEMS_MAX];
    }

    @Override
    protected void quit() {
        cheatMobs = null;
        cheatOres = null;
        cheatType = null;
        cheatMark = null;
        cheatDamage = null;
    }

    @Override
    protected void updateConfig() {
        tagCheat = getOptionString("tagCheater");
        tagMobs = getOptionString("tagCheatShowMobs");
        tagOres = getOptionString("tagCheatShowOres");
        boolean prev = false;
        optShowHealth = false;
        //optCheatShowHealth = getSetBool(optCheatShowHealth, "optCheatShowHealth", true, "Show critter health");
        keyRemoveFire = Keyboard.KEY_NONE;
        //keyCheatRemoveFire = getSetBind(keyCheatRemoveFire, "keyCheatRemoveFire",    Keyboard.KEY_N, "Remove fire nearby");

        keyCheat     = getOptionKey("keyCheat");
        keyHighlight = getOptionKey("keyCheatHighlight");
        keyHealth    = getOptionKey("keyCheatHealth");
        keyDamage    = getOptionKey("keyCheatDamage");
        keyShowMobs  = getOptionKey("keyCheatShowMobs");
        keyShowOres  = getOptionKey("keyCheatShowOres");
        keySee       = getOptionKey("keyCheatSee");
        optShowMobsRange  = getOptionFloat("optCheatShowMobsRange");
        optShowOresRangeH = getOptionInt("optCheatShowOresRangeH");
        optShowOresRangeV = getOptionInt("optCheatShowOresRangeV");
        optSeeDist       = getOptionFloat("optCheatSeeDist");
        optCheat         = getOptionBool("optCheat");
        optSeeIsToggle   = getOptionBool("optCheatSeeIsToggle");
        optShowMobsSize  = getOptionBool("optCheatShowMobsSize");
        optRestoreHealth = getOptionBool("optCheatRestoreHealth");
        optDisableDamage = getOptionBool("optCheatDisableDamage");
        optFallDamage    = getOptionBool("optCheatFallDamage");
        optFireImmune    = getOptionBool("optCheatFireImmune");
        optNerfEnderman  = getOptionBool("optCheatNerfEnderman");
        optNoAir         = getOptionBool("optCheatNoAir");

        optCheatInfArrows = getOptionBool("optCheatInfArrows");

        optInfArmor = false;
        //optCheatInfArmor = getSetBool(optCheatInfArmor, "optCheatInfArmor", false, "Indestructible armor");
        //for (int i=298;i<=317;i++) cheatDamage[i] = optInfArmor;
        optInfSword = false;
        //optCheatInfSword = getSetBool(optCheatInfSword, "optCheatInfSword", false, "Indestructible sword/bow");
        //cheatDamage[267] = cheatDamage[268] = cheatDamage[272] = cheatDamage[276] = cheatDamage[283] = cheatDamage[261] = optInfSword;
        optInfTools = false;
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
        cheatMobs = new Color[MOBS_MAX];
        cheatOres = new Color[ORES_MAX];
        Map<Integer,Color> colormap;
        colormap = parseEntityColorMap(getOptionString("optCheatShowMobs"));
        for (Map.Entry<Integer,Color> entry : colormap.entrySet()) {
            int id = entry.getKey();
            if (0 <= id && id < MOBS_MAX) cheatMobs[id] = entry.getValue();
        }
        colormap = parseBlockColorMap(getOptionString("optCheatShowOres"));
        for (Map.Entry<Integer,Color> entry : colormap.entrySet()) {
            int id = entry.getKey();
            if (0 <= id && id < ORES_MAX) cheatOres[id] = entry.getValue();
        }
    }

    @Override
    protected void onWorldChange() {
        cheating = optCheat && modCheatAllowed;
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        modCheatAllowed = ZHandle.handle("allowCheats", true);

        if (getGamma() < 100f) cheatGamma = getGamma();
        setGamma((cheating && cheatHighlight && (!isInMenu() || getMenu() instanceof GuiChat || getMenu() instanceof GuiContainer)) ? 1000f : cheatGamma);

        boolean enable = optNerfEnderman && !isMultiplayer();
        if (enable != cheatCarryOverride)
        try {
            cheatCarryOverride = enable;
            Object arr = getValue(fCarryBlocks, null);
            if (cheatCarryBlocks == null) {
                cheatCarryBlocks = new boolean[256];
                for (int i=0;i<256;i++) cheatCarryBlocks[i] = Array.getBoolean(arr, i);
            }
            for (int i=0;i<256;i++) Array.setBoolean(arr, i, !enable && cheatCarryBlocks[i]);
        } catch (Exception e) {}

        if (!isInMenu() && wasKeyPressedThisTick(keyCheat)) {
            cheating = !cheating;
            if (!modCheatAllowed && cheating) {
                cheating = false;
                chatClient("\u00a74zombe's \u00a72cheat\u00a74-mod is not allowed on this server.");
            }
        }

        if (!cheating) return;
        if (!isInMenu()) {
            if (wasKeyPressedThisTick(keyShowMobs)) cheatShowMobs = !cheatShowMobs;
            if (wasKeyPressedThisTick(keyShowOres)) cheatShowOres = !cheatShowOres;
            if (wasKeyPressedThisTick(keyHighlight)) cheatHighlight = !cheatHighlight;
            if (wasKeyPressedThisTick(keyHealth)) optRestoreHealth = !optRestoreHealth;
            if (wasKeyPressedThisTick(keyDamage)) optDisableDamage = !optDisableDamage;
            if (optSeeIsToggle) {
                if (wasKeyPressedThisTick(keySee)) cheatSee = !cheatSee;
            } else cheatSee = isKeyDownThisTick(keySee);
            if (!isMultiplayer() && wasKeyPressedThisTick(keyRemoveFire)) {
                BlockPos pos = getPos(getView());
                int x = getX(pos), y = getY(pos), z = getZ(pos);
                World world = getWorld();
                for (int dx = -16; dx <= 16; ++dx)
                for (int dy = -16; dy <= 16; ++dy)
                for (int dz = -16; dz <= 16; ++dz)
                if (getIdAt(world,x+dx,y+dy,z+dz) == 51)
                    setIdAt(world,0,MARK_BOTH,x+dx,y+dy,z+dz);
            }
        }
        if (!isMultiplayer()) {
            /*
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
            */
        }
    }

    @Override
    protected void onWorldDraw(float delta, float x, float y, float z) {
        if (!cheatShowMobs && !cheatShowOres && !optShowHealth) return;
        List list = getEntities();
        Entity view = getView();

        double px = x, py = y, pz = z, mx, my, mz, dx, dy, dz;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (cheating && optShowHealth && !isMultiplayer()) {
            GL11.glColor3ub((byte)0, (byte)128, (byte)0);
            GL11.glBegin(GL11.GL_QUADS);
            for (Object obj : list) {
                if (!(obj instanceof EntityLiving) || obj == view) continue;
                EntityLiving ent = (EntityLiving) obj;
                Vec3d pos = getPositionDelta(ent, delta);
                float health = getHealth(ent);
                mx = getX(pos);
                my = getY(pos) + getYFix(ent) + getHeight(ent);
                mz = getZ(pos);
                dx = mz - pz;
                dz = -(mx - px);
                double d = Math.sqrt(dx*dx + dz*dz);
                double w = 0.25 * health;
                mx -= x; my -= y; mz -= z;
                if (d < 0.1 || d > 64) continue;
                dx /= d; dz /= d;
                while (health>0) {
                    double ax1, ax2, az1, az2;
                    w -= 0.3;
                    ax1 = w * dx * 0.1; az1 = w * dz * 0.1;
                    w -= 0.7;
                    ax2 = w * dx * 0.1; az2 = w * dz * 0.1;
                    GL11.glVertex3d(mx+ax1, my-0.1, mz+az1);
                    GL11.glVertex3d(mx+ax2, my-0.1, mz+az2);
                    GL11.glVertex3d(mx+ax2, my+(health == 1 ? 0 : 0.1), mz+az2);
                    GL11.glVertex3d(mx+ax1, my+(health == 1 ? 0 : 0.1), mz+az1);
                    health -= 2;
                }
            }
            GL11.glEnd();
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_FOG);
        if (modCheatAllowed && cheating && cheatShowMobs) {
            float range = optShowMobsRange * optShowMobsRange;
            //GL11.glEnable(GL11.GL_LINE_STIPPLE);
            //GL11.glLineStipple(1, (short)0x5555);
            GL11.glBegin(GL11.GL_LINES);
            for (Object obj : list) {
                Entity ent = (Entity) obj;
                int id = getId(ent);
                Color col = (0 <= id && id < MOBS_MAX) ? cheatMobs[id] : null;
                if (col == null || obj == view) continue;
                Vec3d pos = getPositionDelta(ent, delta);
                mx = getX(pos);
                my = getY(pos) + getYFix(ent);
                mz = getZ(pos);
                dx = mx - x; dy = my - y; dz = mz - z;
                if (optShowMobsRange > 0 && dx*dx + dy*dy + dz*dz > range) continue;
                float height = (optShowMobsSize || !(ent instanceof EntityLiving)) ? getHeight(ent) : 2.0f;
                GL11.glColor3ub(col.rb, col.gb, col.bb);
                GL11.glVertex3d(dx,dy,dz);
                GL11.glVertex3d(dx,dy+height,dz);
            }
            GL11.glEnd();
            //GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }
        GL11.glBegin(GL11.GL_LINES);
        if (modCheatAllowed && cheating && cheatShowOres) {
            if (--cheatUpdate < 0) {
                cheatUpdate = 17;
                cheatReCheck(fix(x), fix(y), fix(z));
            }
            for (int i = 0; i < cheatCur; ++i) {
                Color col = cheatType[i];
                Vec3d  pos = cheatMark[i];
                if (col == null || pos == null) continue;
                GL11.glColor3ub(col.rb, col.gb, col.bb);
                mx = getX(pos) - x; my = getY(pos) - y; mz = getZ(pos) - z;
                GL11.glVertex3d(mx+0.25,my+0.25,mz+0.25);
                GL11.glVertex3d(mx-0.25,my-0.25,mz-0.25);
                GL11.glVertex3d(mx+0.25,my+0.25,mz-0.25);
                GL11.glVertex3d(mx-0.25,my-0.25,mz+0.25);
                GL11.glVertex3d(mx+0.25,my-0.25,mz+0.25);
                GL11.glVertex3d(mx-0.25,my+0.25,mz-0.25);
                GL11.glVertex3d(mx+0.25,my-0.25,mz-0.25);
                GL11.glVertex3d(mx-0.25,my+0.25,mz+0.25);
            }
        }
        GL11.glEnd();
    }

    @Override
    protected String getTag() {
        if (!modCheatAllowed || !cheating) return null;
        String tag = tagCheat;
        if (cheatShowMobs) tag += ' '+tagMobs;
        if (cheatShowOres) tag += ' '+tagOres;
        return tag;
    }

    @Override
    protected Object handle(String name, Object arg) {
        if (name.equals("isCheating"))
            return (Boolean) isCheating();
        if (name.equals("onServerUpdate"))
            onServerUpdate((EntityPlayerMP) arg);
        if (name.equals("afterPlayerMove"))
            afterPlayerMove();
        if (name.equals("onSortAndRender"))
            startCheatRender();
        return arg;
    }

    private static void cheatReCheck(int pX, int pY, int pZ) {
        cheatCur = 0;
        World world = getWorld();
        for (int x = pX - optShowOresRangeH; x < pX + optShowOresRangeH; ++x)
        for (int y = pY - optShowOresRangeV; y < pY + optShowOresRangeV; ++y)
        for (int z = pZ - optShowOresRangeH; z < pZ + optShowOresRangeH; ++z) {
            int id = getIdAt(world, x,y,z);
            if (id < 0 || id > cheatOres.length) continue;
            Color color = cheatOres[id];
            if (color == null) continue;
            cheatType[cheatCur] = color;
            cheatMark[cheatCur] = new Vec3d(x+0.5f,y+0.5f,z+0.5f);
            ++cheatCur;
            if (cheatCur >= cheatMark.length) return;
        }
    }

    private static void onServerUpdate(EntityPlayerMP ent) {
        if (!modCheatAllowed) return;
        ent.capabilities.disableDamage = cheating && optDisableDamage || ent.capabilities.isCreativeMode;
        setFireImmune(ent, cheating && optFireImmune);
        if (cheating && optRestoreHealth && getHealth(ent) < getMaxHealth(ent))
            setHealth(ent, getHealth(ent)+1);
        if (cheating && optNoAir) setAir(ent, 300);
        if (cheating && !optFallDamage) setFall(ent, 0);
    }

    private static void afterPlayerMove() {
        if (cheating && !optFallDamage) {
            setFall(getPlayer(), 0f);
            setOnGround(getPlayer(), true);
        }
    }

    private static boolean isCheating(){
        return cheating;
    }

    private static void startCheatRender() {
        try {
            if (modCheatAllowed && cheating && cheatSee)
                GuiHelper.setObliqueNearPlaneClip(0.0f, 0.0f, -1.0f, -optSeeDist);
        } catch (Exception error) {
            err("Cheat: see-through setup failed", error);
        }
    }
}
