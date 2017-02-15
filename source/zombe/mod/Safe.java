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

    @Nullable private Color optDangerColor;
    @Nullable private Color optDangerColorSun;
    @Nullable private Vec3i safePoses[];
    @Nullable private Color safeMarks[];
    @Nullable private String tagSafe;

    private int safeCur, safeUpdate;
    private int keyShow, keyGhost;
    private int optLookupRadius;
    private boolean optShowWithSun;
    private boolean safeShow;
    private boolean safeGhost;

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

    private boolean emptySpaceHere(int pX, int pY, int pZ) {
        double x = pX + 0.5, y = (double) pY, z = pZ + 0.5;
        //double r = 0.3, h = 1.8; // skeleton size
        //double r = 0.35, h = 0.5; // cave spider size
        double r = 0.3, h = 0.5; // hybrid size
        AxisAlignedBB aabb = new AxisAlignedBB(x - r, y, z - r, x + r, y + h, z + r);
        World world = getWorld();
        return getCollidingBlockAABBs(world, aabb).isEmpty() && !world.containsAnyLiquid(aabb);
    }

    private boolean couldSpawnHere(int x, int y, int z) {
        try {
            return y >= 0 && getBlockLightLevel(x, y, z) < 8 && canMonsterSpawnAt(x, y, z) && this.emptySpaceHere(x, y, z);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void init() {
        this.safePoses = new Vec3i[SAFE_MAX];
        this.safeMarks = new Color[SAFE_MAX];
        this.safeCur = 0;
        this.safeUpdate = 0;
        this.safeShow = false;
        this.safeGhost = false;
    }

    @Override
    protected void quit() {
        this.safePoses = null;
        this.safeMarks = null;
    }

    @Override
    protected void updateConfig() {
        this.tagSafe = getOptionString("tagSafe");
        this.keyShow = getOptionKey("keySafeShow");
        this.keyGhost = getOptionKey("keySafeGhost");
        this.optLookupRadius = getOptionInt("optSafeLookupRadius");
        this.optShowWithSun = getOptionBool("optSafeShowWithSun");
        this.optDangerColor = getOptionColor("optSafeDangerColor");
        this.optDangerColorSun = getOptionColor("optSafeDangerColorSun");
    }

    @Override
    protected void onWorldChange() {
        this.safeCur = 0;
        this.safeUpdate = 0;
    }

    @Override
    protected void onClientTick(EntityPlayerSP player) {
        if (isInMenu()) {
            return;
        }
        if (wasKeyPressedThisTick(this.keyShow)) {
            this.safeShow = !this.safeShow;
        }
        if (wasKeyPressedThisTick(this.keyGhost)) {
            this.safeGhost = !this.safeGhost;
        }
    }

    @Override
    protected void onWorldDraw(float delta, float x, float y, float z) {
        assert this.safePoses != null && this.safeMarks != null;

        float mx, my, mz;
        if (!this.safeShow) {
            return;
        }
        if (--this.safeUpdate < 0) {
            this.safeUpdate = 13;
            this.reCheckSafe(fix(x), fix(y), fix(z));
        }
        if (this.safeGhost) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < this.safeCur; ++i) {
            Vec3i pos = this.safePoses[i];
            Color color = this.safeMarks[i];
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

    @Nullable
    @Override
    protected String getTag() {
        if (!this.safeShow || this.tagSafe == null || this.tagSafe.isEmpty()) {
            return null;
        }

        return this.tagSafe;
    }

    private void reCheckSafe(int pX, int pY, int pZ) {
        assert this.safePoses != null && this.safeMarks != null;

        this.safeCur = 0;
        for (int x = pX - this.optLookupRadius; x <= pX + this.optLookupRadius; ++x) {
            for (int y = pY - this.optLookupRadius; y <= pY + this.optLookupRadius; ++y) {
                for (int z = pZ - this.optLookupRadius; z <= pZ + this.optLookupRadius; ++z) {
                    if (this.couldSpawnHere(x, y, z)) {
                        this.safePoses[this.safeCur] = new Vec3i(x, y, z);
                        this.safeMarks[this.safeCur] = (this.optShowWithSun && getSkyLightLevel(x, y, z) > 7) ? this.optDangerColorSun
                                                                                                              : this.optDangerColor;
                        this.safeCur++;
                        if (this.safeCur == SAFE_MAX) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
