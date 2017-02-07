package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import zombe.core.ZMod;
import zombe.core.util.Color;

import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Safe extends ZMod {

    private static final int SAFE_MAX = 2048;
    private static String tagSafe;
    private static int keyShow, keyGhost;
    @Nullable private static Color optDangerColor, optDangerColorSun;
    private static boolean optShowWithSun;
    private static int optLookupRadius;
    @Nullable private static Vec3i safePoses[];
    @Nullable private static Color safeMarks[];
    private static boolean safeShow;
    private static boolean safeGhost;
    private static int safeCur, safeUpdate;

    public Safe() {
        super("safe", "1.8", "9.0.0");
        this.addOption("tagSafe", "Mod tag", "safe");
        this.addOption("keySafeShow", "Show / hide un-safe markers", Keyboard.KEY_L);
        this.addOption("keySafeGhost", "Toggle show marks through walls", Keyboard.KEY_K);
        this.addOption("optSafeLookupRadius", "Un-safe lookup radius", 16, 0, 64);
        this.addOption("optSafeShowWithSun", "Mark 'safe at midday' differently", true);
        this.addOption("optSafeDangerColor", "Marks color", "0xff0000");
        this.addOption("optSafeDangerColorSun", "Marks color (sun)", "0xdddd00");
    }

    private static boolean emptySpaceHere(int pX, int pY, int pZ) {
        double x = pX + 0.5, y = (double) pY, z = pZ + 0.5;
        //double r = 0.3, h = 1.8; // skeleton size
        //double r = 0.35, h = 0.5; // cave spider size
        double r = 0.3, h = 0.5; // hybrid size
        AxisAlignedBB aabb = new AxisAlignedBB(x - r, y, z - r, x + r, y + h, z + r);
        World world = getWorld();
        return getCollidingBlockAABBs(world, aabb).isEmpty() && !world.containsAnyLiquid(aabb);
    }

    private static boolean couldSpawnHere(int x, int y, int z) {
        try {
            return y >= 0 && getBlockLightLevel(x, y, z) < 8 && canMonsterSpawnAt(x, y, z) && emptySpaceHere(x, y, z);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void init() {
        safePoses = new Vec3i[SAFE_MAX];
        safeMarks = new Color[SAFE_MAX];
        safeCur = 0;
        safeUpdate = 0;
        safeShow = false;
        safeGhost = false;
    }

    @Override
    protected void quit() {
        safePoses = null;
        safeMarks = null;
    }

    @Override
    protected void updateConfig() {
        tagSafe = getOptionString("tagSafe");
        keyShow = getOptionKey("keySafeShow");
        keyGhost = getOptionKey("keySafeGhost");
        optLookupRadius = getOptionInt("optSafeLookupRadius");
        optShowWithSun = getOptionBool("optSafeShowWithSun");
        optDangerColor = getOptionColor("optSafeDangerColor");
        optDangerColorSun = getOptionColor("optSafeDangerColorSun");
    }

    @Override
    protected void onWorldChange() {
        safeCur = 0;
        safeUpdate = 0;
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        if (isInMenu()) {
            return;
        }
        if (wasKeyPressedThisTick(keyShow)) {
            safeShow = !safeShow;
        }
        if (wasKeyPressedThisTick(keyGhost)) {
            safeGhost = !safeGhost;
        }
    }

    @Override
    protected void onWorldDraw(float delta, float x, float y, float z) {
        float mx, my, mz;
        if (!safeShow) {
            return;
        }
        if (--safeUpdate < 0) {
            safeUpdate = 13;
            this.reCheckSafe(fix(x), fix(y), fix(z));
        }
        if (safeGhost) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < safeCur; ++i) {
            Vec3i pos = safePoses[i];
            Color color = safeMarks[i];
            GL11.glColor3ub(color.rb, color.gb, color.bb);
            mx = pos.getX() - x;
            my = pos.getY() - y;
            mz = pos.getZ() - z;
            GL11.glVertex3f(mx + 0.9f, my + 0.01f, mz + 0.9f);
            GL11.glVertex3f(mx + 0.1f, my + 0.01f, mz + 0.1f);
            GL11.glVertex3f(mx + 0.9f, my + 0.01f, mz + 0.1f);
            GL11.glVertex3f(mx + 0.1f, my + 0.01f, mz + 0.9f);
        }
        GL11.glEnd();
    }

    @Override
    protected String getTag() {
        if (!safeShow || tagSafe.length() == 0) {
            return null;
        }
        return tagSafe;
    }

    private void reCheckSafe(int pX, int pY, int pZ) {
        safeCur = 0;
        for (int x = pX - optLookupRadius; x <= pX + optLookupRadius; ++x) {
            for (int y = pY - optLookupRadius; y <= pY + optLookupRadius; ++y) {
                for (int z = pZ - optLookupRadius; z <= pZ + optLookupRadius; ++z) {
                    if (couldSpawnHere(x, y, z)) {
                        safePoses[safeCur] = new Vec3i(x, y, z);
                        safeMarks[safeCur] = (optShowWithSun && getSkyLightLevel(x, y, z) > 7) ? optDangerColorSun : optDangerColor;
                        ++safeCur;
                        if (safeCur == SAFE_MAX) {
                            return;
                        }
                    }
                }
            }
        }
    }

}
