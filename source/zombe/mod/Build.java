package zombe.mod;


import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import zombe.core.ZMod;
import zombe.core.util.BlockFace;
import zombe.core.util.GuiHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

import static zombe.core.ZWrapper.*;

public final class Build extends ZMod {

    private static String tagBuild;
    private static int keyToggle, keyA, keyB, keyMark, keyCopy, keyPaste, keySet, keyFill, keyRemove, keyFeet, keyHead, keyPick, keyDeselect;
    private static float optLockQuantityRatio;
    private static boolean optBuild, optExtension, optLockQuantity;

    private static boolean building, buildBufferHere;
    private static int buildSX, buildSY, buildSZ,
                       buildEX, buildEY, buildEZ,
                       buildX1, buildY1, buildZ1,
                       buildX2, buildY2, buildZ2,
                       buildMark = 0;
    private static int bufferSX, bufferSY, bufferSZ,
                       bufferEX, bufferEY, bufferEZ,
                       bufferWX, bufferWY, bufferWZ;
    private static int buildHandSlot, buildHandSize;
    @Nullable private static int buildSets[][], buildBuffer[] = null;
    @Nullable private static NBTTagCompound buildBufferNBT[];
    @Nullable private static ItemStack buildHand;
    @Nullable private static List<int[]> serverActions = null;
    @Nullable private static List<int[]> clientActions = null;

    private static final int
        BUILD_ACTION_BITS = 7,
        BUILD_ACTION_UPDATE = 0,
        BUILD_ACTION_SET = 1,
        BUILD_ACTION_COPY = 2,
        BUILD_ACTION_PASTE = 3,
        BUILD_ACTION_ITEMSET = 4,
        BUILD_MODIFIER_FILL = 64,
        BUILD_MODIFIER_REMOVE = 128;

    public Build() {
        super("build", "1.8", "9.0.2");

        this.addOption("tagBuild", "Mod tag", "builder");
        this.addOption("keyBuildToggle", "Toggle builder mode", Keyboard.KEY_B);
        this.addOption("optBuild", "Builder mode is enabled by default", false);
        this.addOption("optBuildExtension", "Build extension (selection) enabled", true);
        this.addOption("keyBuildPick", "Set area marker on target", Keyboard.KEY_X);
        this.addOption("keyBuildMark", "Set area marker at your position", Keyboard.KEY_X);
        this.addOption("keyBuildHead", "Modifier to set marker at head level", Keyboard.KEY_LSHIFT);
        this.addOption("keyBuildFeet", "Modifier to set marker at feet level", Keyboard.KEY_LCONTROL);
        this.addOption("keyBuildDeselect", "Unset selection / marker", Keyboard.KEY_NONE);
        this.addOption("keyBuildCopy", "Copy selected area", Keyboard.KEY_C);
        this.addOption("singleplayer-only features");
        this.addOption("keyBuildPaste", "Paste into selected area", Keyboard.KEY_P);
        this.addOption("keyBuildSet", "Set in selected area", Keyboard.KEY_Z);
        this.addOption("keyBuildFill", "Modifier to fill only empty space", Keyboard.KEY_LSHIFT);
        this.addOption("keyBuildRemove", "Modifier to remove matching", Keyboard.KEY_RSHIFT);
        this.addOption("optBuildLockQuantity", "Lock item quantity", true);
        this.addOption("optBuildLockQuantityRatio", "Lock quantity to ratio (0=don't, 1=full)", 0f, 0f, 1f);
        this.addOption("keyBuildA", "(this + number) load item set A-#", Keyboard.KEY_LSHIFT);
        this.addOption("keyBuildB", "(this + number) load item set B-#", Keyboard.KEY_LCONTROL);
        this.addOption("optBuildA1", "Item set A-1", "");
        this.addOption("optBuildA2", "Item set A-2", "");
        this.addOption("optBuildA3", "Item set A-3", "");
        this.addOption("optBuildA4", "Item set A-4", "");
        this.addOption("optBuildA5", "Item set A-5", "");
        this.addOption("optBuildA6", "Item set A-6", "");
        this.addOption("optBuildA7", "Item set A-7", "");
        this.addOption("optBuildA8", "Item set A-8", "");
        this.addOption("optBuildA9", "Item set A-9", "");
        this.addOption("optBuildB1", "Item set B-1", "");
        this.addOption("optBuildB2", "Item set B-2", "");
        this.addOption("optBuildB3", "Item set B-3", "");
        this.addOption("optBuildB4", "Item set B-4", "");
        this.addOption("optBuildB5", "Item set B-5", "");
        this.addOption("optBuildB6", "Item set B-6", "");
        this.addOption("optBuildB7", "Item set B-7", "");
        this.addOption("optBuildB8", "Item set B-8", "");
        this.addOption("optBuildB9", "Item set B-9", "");
    }

    @Override
    protected void init() {
        building = optBuild;
        buildMark = 0;
        buildHandSlot = -1;
        clientActions = new LinkedList<int[]>();
        serverActions = new LinkedList<int[]>();
    }

    @Override
    protected void quit() {
        synchronized (this) {
            buildSets = null;
            buildBuffer = null;
            buildBufferNBT = null;
            buildHand = null;
            clientActions = null;
            serverActions = null;
        } //synchronized
    }

    @Override
    protected void updateConfig() {
        synchronized(this) {
            tagBuild         = getOptionString("tagBuild");

            keyToggle        = getOptionKey("keyBuildToggle");
            keyA             = getOptionKey("keyBuildA");
            keyB             = getOptionKey("keyBuildB");
            optBuild         = getOptionBool("optBuild");
            optLockQuantity  = getOptionBool("optBuildLockQuantity");
            optLockQuantityRatio = getOptionFloat("optBuildLockQuantityRatio");
            optExtension     = getOptionBool("optBuildExtension");
            keyPick          = getOptionKey("keyBuildPick");
            keyMark          = getOptionKey("keyBuildMark");
            keyCopy          = getOptionKey("keyBuildCopy");
            keyPaste         = getOptionKey("keyBuildPaste");
            keySet           = getOptionKey("keyBuildSet");
            keyFill          = getOptionKey("keyBuildFill");
            keyRemove        = getOptionKey("keyBuildRemove");
            keyHead          = getOptionKey("keyBuildHead");
            keyFeet          = getOptionKey("keyBuildFeet");
            keyDeselect      = getOptionKey("keyBuildDeselect");

            String sets[] = new String[] {
                getOptionString("optBuildA1"),
                getOptionString("optBuildA2"),
                getOptionString("optBuildA3"),
                getOptionString("optBuildA4"),
                getOptionString("optBuildA5"),
                getOptionString("optBuildA6"),
                getOptionString("optBuildA7"),
                getOptionString("optBuildA8"),
                getOptionString("optBuildA9"),
                getOptionString("optBuildB1"),
                getOptionString("optBuildB2"),
                getOptionString("optBuildB3"),
                getOptionString("optBuildB4"),
                getOptionString("optBuildB5"),
                getOptionString("optBuildB6"),
                getOptionString("optBuildB7"),
                getOptionString("optBuildB8"),
                getOptionString("optBuildB9")
            };
            buildSets = new int[sets.length][9];
            for (int set = 0; set < sets.length; ++set) {
                List<Integer> got = parseItemList(sets[set]);
                int defs = got.size();
                if (defs > 9) {
                    defs = 9;
                }
                for (int slot = 0; slot < defs; ++slot) {
                    int idmeta = got.get(slot);
                    if (idmeta == -1) {
                        showOnscreenError("error: option optBuild" + (set > 9 ? "B" : "A") + ((set % 9) + 1) + ", slot " + slot + " - unknown item name or invalid code");
                    }
                    buildSets[set][slot] = idmeta;
                }
                for (int slot = defs; slot < 9; ++slot) {
                    buildSets[set][slot] = 0;
                }
            }
        } //synchronized
    }

    @Override
    protected void onWorldChange() {
        synchronized (this) {
            building = optBuild;
            buildHandSlot = -1;
            buildHand = null;
            buildBufferHere = false;
            clientActions.clear();
            serverActions.clear();
        } //synchronized
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        synchronized (this) {
            if (!building || isInMenu() || !optLockQuantity || isMultiplayer()) {
                buildHandSlot = -1;
            }
            if (isInMenu()) {
                return;
            }
            if (wasKeyPressedThisTick(keyToggle)) {
                building = !building;
            }
            if (!building) {
                buildMark = 0;
                return;
            }

            World world = getWorld(player);

            // sets
            if (isKeyDownThisTick(keyA) || isKeyDownThisTick(keyB)) {
                int set = -1;
                for (int i = Keyboard.KEY_1; i <= Keyboard.KEY_9; ++i) {
                    if (wasKeyPressedThisTick(i)) {
                        set = i - Keyboard.KEY_1;
                    }
                }
                if (set != -1) {
                    if (isKeyDownThisTick(keyB)) {
                        set += 9;
                    }
                    int[] data = new int[10];
                    data[0] = BUILD_ACTION_ITEMSET;
                    for (int slot = 0; slot < 9; ++slot) {
                        data[slot + 1] = buildSets[set][slot];
                    }
                    serverActions.add(data);
                }
            }

            // deselect
            if (wasKeyPressedThisTick(keyDeselect)) {
                if (buildMark == 2) {
                    buildMark = 1;
                    buildSX = buildEX = buildX2 = buildX1;
                    buildSY = buildEY = buildY2 = buildY1;
                    buildSZ = buildEZ = buildZ2 = buildZ1;
                } else {
                    buildMark = 0;
                }
            }

            // action commands
            if (optExtension && (wasKeyPressedThisTick(keyMark) || wasKeyPressedThisTick(keyPick))) {
                // new marker position
                Entity view = getView();
                if (view == null) {
                    view = player;
                }
                int x = fix(getX(view));
                int y = fix(getY(view) + getEyeHeight(view)) - 1;
                int z = fix(getZ(view));
                if (isKeyDownThisTick(keyFeet)) {
                    y -= 1;
                } else if (isKeyDownThisTick(keyHead)) {
                    y += 1;
                } else if (wasKeyPressedThisTick(keyPick)) {
                    BlockFace face = getBlockFace(rayTrace(view, 128, 1f));
                    if (face != null) {
                        x = face.x;
                        y = face.y;
                        z = face.z;
                    }
                }
                // update markers
                if (buildMark <= 0) {
                    buildMark = 1;
                    buildSX = buildEX = buildX2 = buildX1 = x;
                    buildSY = buildEY = buildY2 = buildY1 = y;
                    buildSZ = buildEZ = buildZ2 = buildZ1 = z;
                } else {
                    buildMark = 2;
                    buildX2 = buildX1;
                    buildY2 = buildY1;
                    buildZ2 = buildZ1;
                    buildX1 = x;
                    buildY1 = y;
                    buildZ1 = z;
                    buildSX = Math.min(buildX1, buildX2);
                    buildSY = Math.min(buildY1, buildY2);
                    buildSZ = Math.min(buildZ1, buildZ2);
                    buildEX = Math.max(buildX1, buildX2);
                    buildEY = Math.max(buildY1, buildY2);
                    buildEZ = Math.max(buildZ1, buildZ2);
                }
            } else if (buildMark > 0) {
                if (wasKeyPressedThisTick(keyCopy)) {
                    buildAction(world, player, new int[]{ BUILD_ACTION_COPY, buildSX, buildSY, buildSZ, buildEX, buildEY, buildEZ });
                } else if (wasKeyPressedThisTick(keySet) && !isMultiplayer()) {
                    ItemStack stack = getStacks(player)[getCurrentSlot(player)];
                    int id = getId(stack);
                    int meta = getMeta(stack);
                    Block block = getBlock(stack);
                    if (block == null) {
                        if (id == 326) block = getBlock(9); // water bucket
                        if (id == 327) block = getBlock(11); // lava bucket
                        if (id == 355) block = getBlock(26); // bed
                        if (id == 323) block = getBlock(63); // sign
                        if (id == 259) block = getBlock(51); // flint & steel
                        if (id == 331) block = getBlock(55); // redstone
                        if (id == 356) block = getBlock(93); // repeater
                        if (id == 404) block = getBlock(149); // comparator
                        if (id == 324) block = getBlock(64);  // door oak
                        if (id == 330) block = getBlock(71);  // door iron
                        if (id == 427) block = getBlock(193); // door spruce
                        if (id == 428) block = getBlock(194); // door birch
                        if (id == 429) block = getBlock(195); // door jungle
                        if (id == 430) block = getBlock(196); // door acacia
                        if (id == 431) block = getBlock(197); // door dark oak
                    }
                    if (block != null) {
                        serverActions.add(new int[]{ BUILD_ACTION_SET | (isKeyDownThisTick(keyFill) ? BUILD_MODIFIER_FILL : 0) | (isKeyDownThisTick(keyRemove) ? BUILD_MODIFIER_REMOVE : 0), buildSX, buildSY, buildSZ, buildEX, buildEY, buildEZ, getBlockIdMeta(getId(block), meta) });
                    }
                } else if (wasKeyPressedThisTick(keyPaste) && !isMultiplayer()) {
                    if (buildMark == 1 && buildBuffer != null) {
                        buildX2 = buildEX = buildSX + bufferWX - 1;
                        buildY2 = buildEY = buildSY + bufferWY - 1;
                        buildZ2 = buildEZ = buildSZ + bufferWZ - 1;
                        buildMark = 2;
                    } else if (buildBuffer != null) {
                        serverActions.add(new int[]{ BUILD_ACTION_PASTE | (isKeyDownThisTick(keyFill) ? BUILD_MODIFIER_FILL : 0) | (isKeyDownThisTick(keyRemove) ? BUILD_MODIFIER_REMOVE : 0), buildSX, buildSY, buildSZ, buildEX, buildEY, buildEZ });
                    }
                }
            }

            // build actions
            if (clientActions != null && !clientActions.isEmpty()) {
                for (int[] action : clientActions) {
                    buildAction(world, player, action);
                }
                clientActions.clear();
            }
        } //synchronized
    }

    private static void buildAction(@Nonnull World world, @Nonnull EntityPlayer player, int[] data) {
        final int action = data[0] & BUILD_ACTION_BITS;

        if (action == BUILD_ACTION_ITEMSET) {
            for (int slot = 0; slot < 9; ++slot) {
                int idmeta = data[slot + 1];
                if (idmeta == 0 || idmeta == -1) {
                    getStacks(player)[slot] = null;
                } else {
                    getStacks(player)[slot] = getStack(idmeta, getItemMax(getItem(getBase(idmeta))));
                }
            }
            buildHandSlot = -1;
            return;
        }

        final boolean fill = (data[0] & BUILD_MODIFIER_FILL) != 0;
        final boolean remove = (data[0] & BUILD_MODIFIER_REMOVE) != 0;

        int sx = data[1], sy = data[2], sz = data[3];
        int ex = data[4], ey = data[5], ez = data[6];

        if (action == BUILD_ACTION_UPDATE) {
            markForUpdate(world, sx, sy, sz, ex, ey, ez);
        } else if (action == BUILD_ACTION_COPY) {
            bufferWX = 1 + ex - sx;
            bufferWY = 1 + ey - sy;
            bufferWZ = 1 + ez - sz;
            bufferSX = sx;
            bufferSY = sy;
            bufferSZ = sz;
            bufferEX = ex;
            bufferEY = ey;
            bufferEZ = ez;
            int size = bufferWX * bufferWY * bufferWZ, at = 0;
            buildBuffer = new int[size];
            buildBufferNBT = new NBTTagCompound[size];
            buildBufferHere = true;
            for (int x = sx; x <= ex; ++x) {
                for (int y = sy; y <= ey; ++y) {
                    for (int z = sz; z <= ez; ++z) {
                        buildBuffer[at] = getIdMetaAt(world, x, y, z);
                        buildBufferNBT[at] = getTileEntityCopy(getTileEntityAt(world, x, y, z));
                        ++at;
                    }
                }
            }
        } else if (action == BUILD_ACTION_SET) {
            int idmeta = data[7];
            int id = getBlockId(idmeta);
            boolean sub = hasSubTypes(getItem(getBlock(id)));
            //int meta = data[8];

            if (fill) {
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            if (getIdAt(world, x, y, z) == 0) {
                                setIdMetaAt(world, idmeta, UPDATE_NONE, x, y, z);
                            }
                        }
                    }
                }
            } else if (remove) {
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            int gotmeta = getIdMetaAt(world, x, y, z);
                            int got = getBlockId(gotmeta);
                            if (gotmeta == idmeta || !sub && (got == id
                                 || id ==  8 && got ==  9 || id == 10 && got == 11
                                 || id == 76 && got == 75 || id == 93 && got == 94
                                 || id == 61 && got == 62 || id ==123 && got ==124)) {
                                setIdAt(world, 0, UPDATE_NONE, x, y, z);
                            }
                        }
                    }
                }
            } else {
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            setIdMetaAt(world, idmeta, UPDATE_NONE, x, y, z);
                        }
                    }
                }
            }
            notifyAndMark(world, sx, sy, sz, ex, ey, ez);
            //clientActions.add(new int[] { BUILD_ACTION_UPDATE,
            //                  sx,sy,sz, ex,ey,ez });
        } else if (action == BUILD_ACTION_PASTE && buildBuffer != null) {
            int wx = 1 + ex - sx; wx = (wx > bufferWX) ? wx % bufferWX : 0;
            int wy = 1 + ey - sy; wy = (wy > bufferWY) ? wy % bufferWY : 0;
            int wz = 1 + ez - sz; wz = (wz > bufferWZ) ? wz % bufferWZ : 0;
            if (wx != 0 || wy != 0 || wz != 0) {
                ex -= wx;
                ey -= wy;
                ez -= wz;
                // assert : (1+ex-sx) % bufferWX == 0 || (1+ex-sx) < bufferWX
            }
            if (fill) { // fill space
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            if (getIdAt(world, x, y, z) == 0) {
                                int cx = (x - sx) % bufferWX;
                                int cy = (y - sy) % bufferWY;
                                int cz = (z - sz) % bufferWZ;
                                int at = (cx * bufferWY + cy) * bufferWZ + cz;

                                setIdMetaAt(world, buildBuffer[at], UPDATE_NONE, x, y, z);
                                if (buildBufferNBT[at] != null) {
                                    setTileEntityFromCopy(world, x, y, z, buildBufferNBT[at]);
                                    setChanged(getTileEntityAt(world, x, y, z));
                                }
                            }
                        }
                    }
                }
            } else if (remove) { // remove matching
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            int cx = (x - sx) % bufferWX,
                                    cy = (y - sy) % bufferWY,
                                    cz = (z - sz) % bufferWZ;
                            int at = (cx * bufferWY + cy) * bufferWZ + cz;
                            int idmeta = buildBuffer[at], got = getIdMetaAt(world, x, y, z);
                            if (idmeta == got || (idmeta == 8 && got == 9) || (idmeta == 10 && got == 11)) {
                                setIdAt(world, 0, UPDATE_NONE, x, y, z);
                            }
                        }
                    }
                }
            } else { // replace
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        for (int z = sz; z <= ez; ++z) {
                            int cx = (x - sx) % bufferWX;
                            int cy = (y - sy) % bufferWY;
                            int cz = (z - sz) % bufferWZ;
                            int at = (cx * bufferWY + cy) * bufferWZ + cz;

                            setIdMetaAt(world, buildBuffer[at], UPDATE_NONE, x, y, z);
                            if (buildBufferNBT[at] != null) {
                                setTileEntityFromCopy(world, x, y, z, buildBufferNBT[at]);
                                setChanged(getTileEntityAt(world, x, y, z));
                            }
                        }
                    }
                }
            }
            notifyAndMark(world, sx, sy, sz, ex, ey, ez);
            //clientActions.add(new int[] { BUILD_ACTION_UPDATE,
            //                  sx,sy,sz, ex,ey,ez });
        }
    }

    @Override
    protected void onServerTick(@Nonnull EntityPlayerMP ent) {
        synchronized (this) {
            // build actions
            World world = getWorld(ent);
            if (serverActions != null && !serverActions.isEmpty()) {
                for (int[] action : serverActions) {
                    try {
                        buildAction(world, ent, action);
                    } catch (Exception e) {
                        showOnscreenError("In Build: a build action ("+action[0]+") failed",e);
                    }
                }
                serverActions.clear();
            }

            // lock items in hand
            if (building && optLockQuantity) {
                ItemStack[] stacks = getStacks(ent);
                int cur = getCurrentSlot(ent);
                if (cur != buildHandSlot || (stacks[cur] != null && stacks[cur] != buildHand)) {
                    buildHandSlot = cur;
                    buildHand     = stacks[cur];
                    buildHandSize = buildHand != null ? getStackSize(buildHand) : 0;
                } else if (buildHand != null && (stacks[cur] == null || stacks[cur] == buildHand)) {
                    int size = buildHandSize;
                    if (optLockQuantityRatio > 0) {
                        int max = getItemMax(getItem(buildHand));
                        size = (int) Math.round(Math.ceil(max * optLockQuantityRatio));
                    }
                    setStackSize(buildHand, buildHandSize);
                    setStack(ent, cur, buildHand);
                }
            }
        } //synchronized
    }

    @Override
    protected void onWorldDraw(float delta, float x, float y, float z) {
        float sx, sy, sz, ex, ey, ez;
        // change state
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        // draw selection box sides
        if (buildMark == 2) {
            // calculate selection box
            sx = buildSX - x - 0.1f; ex = buildEX - x + 1.1f;
            sy = buildSY - y - 0.1f; ey = buildEY - y + 1.1f;
            sz = buildSZ - z - 0.1f; ez = buildEZ - z + 1.1f;
            // draw selection box
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4ub((byte) 255, (byte) 64, (byte) 32, (byte) 32);
            GuiHelper.drawQuadBox(sx, sy, sz, ex, ey, ez);
            GL11.glDisable(GL11.GL_BLEND);
            // draw selection box
            GL11.glColor3ub((byte) 32, (byte) 64, (byte) 255);
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // draw buffer box source
        if (buildBuffer != null && buildBufferHere) {
            sx = bufferSX -x - 0.06f; ex = bufferEX -x + 1.06f;
            sy = bufferSY -y - 0.06f; ey = bufferEY -y + 1.06f;
            sz = bufferSZ -z - 0.06f; ez = bufferEZ -z + 1.06f;
            GL11.glColor3ub((byte) 192,(byte) 192,(byte) 192);
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // draw buffer box destination
        if (buildBuffer != null && buildMark > 0 && !isMultiplayer()) {
            sx = buildSX -x - 0.08f; ex = buildSX + bufferWX -x + 0.08f;
            sy = buildSY -y - 0.08f; ey = buildSY + bufferWY -y + 0.08f;
            sz = buildSZ -z - 0.08f; ez = buildSZ + bufferWZ -z + 0.08f;
            if (buildEX-buildSX+1 == bufferWX
                 && buildEY-buildSY+1 == bufferWY
                 && buildEZ-buildSZ+1 == bufferWZ) {
                GL11.glColor3ub((byte) 64, (byte) 255, (byte) 64);
            } else if (buildMark == 1) {
                GL11.glColor3ub((byte) 192, (byte) 32, (byte) 255);
            } else {
                GL11.glColor3ub((byte) 255, (byte) 64, (byte) 64);
            }
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // draw marker 1
        if (buildMark > 0) {
            sx = buildX1 -x -0.04f; ex = buildX1 -x + 1.04f;
            sy = buildY1 -y -0.04f; ey = buildY1 -y + 1.04f;
            sz = buildZ1 -z -0.04f; ez = buildZ1 -z + 1.04f;
            GL11.glColor3ub((byte) 0, (byte) 255, (byte) 255);
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // restore state
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    protected String getTag() {
        if (!building || tagBuild.length() == 0) {
            return null;
        }
        return tagBuild;
    }
}

