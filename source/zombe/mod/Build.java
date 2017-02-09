package zombe.mod;


import com.google.common.base.Function;
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
import zombe.core.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static zombe.core.ZWrapper.*;

public final class Build extends ZMod {

    @Nonnull private static String[] sets = new String[] {
            "optBuildA1", "optBuildA2", "optBuildA3",
            "optBuildA4", "optBuildA5", "optBuildA6",
            "optBuildA7", "optBuildA8", "optBuildA9",
            "optBuildB1", "optBuildB2", "optBuildB3",
            "optBuildB4", "optBuildB5", "optBuildB6",
            "optBuildB7", "optBuildB8", "optBuildB9"
    };

    private String tagBuild;
    private int keyToggle, keyA, keyB, keyMark,
                keyCopy, keyPaste, keySet, keyFill,
                keyRemove, keyFeet, keyHead, keyPick, keyDeselect;
    private float optLockQuantityRatio;
    private boolean optBuild, optExtension, optLockQuantity;

    private boolean building, buildBufferHere;
    private int buildSX, buildSY, buildSZ,
                buildEX, buildEY, buildEZ,
                buildX1, buildY1, buildZ1,
                buildX2, buildY2, buildZ2,
                buildMark = 0;
    private int bufferSX, bufferSY, bufferSZ,
                bufferEX, bufferEY, bufferEZ,
                bufferWX, bufferWY, bufferWZ;
    private int buildHandSlot, buildHandSize;

    @Nonnull int[][] buildSets = new int[sets.length][9];
    @Nonnull ArrayList<Integer> buildBuffer = new ArrayList<>();
    @Nonnull ArrayList<NBTTagCompound> buildBufferNBT = new ArrayList<>();
    @Nonnull ItemStack buildHand = ItemStack.EMPTY;
    @Nonnull List<int[]> serverActions = new ArrayList<>();
    @Nonnull List<int[]> clientActions = new ArrayList<>();

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
        this.building = this.optBuild;
        this.buildMark = 0;
        this.buildHandSlot = -1;
    }

    @Override
    protected void quit() {
        synchronized (this) {
            this.buildBuffer.clear();
            this.buildBufferNBT.clear();
            this.buildHand = ItemStack.EMPTY;
            this.serverActions.clear();
            this.clientActions.clear();
        }
    }

    @Override
    protected void updateConfig() {
        synchronized(this) {
            this.tagBuild = getOptionString("tagBuild");

            this.optLockQuantityRatio = getOptionFloat("optBuildLockQuantityRatio");
            this.optBuild = getOptionBool("optBuild");
            this.optLockQuantity = getOptionBool("optBuildLockQuantity");
            this.optExtension = getOptionBool("optBuildExtension");
            this.keyToggle = getOptionKey("keyBuildToggle");
            this.keyA = getOptionKey("keyBuildA");
            this.keyB = getOptionKey("keyBuildB");
            this.keyPick = getOptionKey("keyBuildPick");
            this.keyMark = getOptionKey("keyBuildMark");
            this.keyCopy = getOptionKey("keyBuildCopy");
            this.keyPaste = getOptionKey("keyBuildPaste");
            this.keySet = getOptionKey("keyBuildSet");
            this.keyFill = getOptionKey("keyBuildFill");
            this.keyRemove = getOptionKey("keyBuildRemove");
            this.keyHead = getOptionKey("keyBuildHead");
            this.keyFeet = getOptionKey("keyBuildFeet");
            this.keyDeselect = getOptionKey("keyBuildDeselect");

            String setsOptions[] = ArrayHelper.map(String.class, sets, new Function<String, String>() {
                @Nullable @Override public String apply(String s) {
                    return getOptionString(s);
                }
            });

            for (int set = 0; set < setsOptions.length; set++) {
                List<Integer> got = parseItemList(setsOptions[set]);
                int defs = got.size();
                if (defs > 9) {
                    defs = 9;
                }
                for (int slot = 0; slot < defs; slot++) {
                    int idmeta = got.get(slot);
                    if (idmeta == -1) {
                        showOnscreenError("error: option optBuild" + (set > 9 ? "B" : "A") + ((set % 9) + 1) +
                                ", slot " + slot + " - unknown item name or invalid code");
                    }
                    this.buildSets[set][slot] = idmeta;
                }
                for (int slot = defs; slot < 9; slot++) {
                    this.buildSets[set][slot] = 0;
                }
            }
        } //synchronized
    }

    @Override
    protected void onWorldChange() {
        synchronized (this) {
            this.building = this.optBuild;
            this.buildHandSlot = -1;
            this.buildHand = ItemStack.EMPTY;
            this.buildBufferHere = false;
            this.clientActions.clear();
            this.serverActions.clear();
        }
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        synchronized (this) {
            if (!this.building || isInMenu() || !this.optLockQuantity || isMultiplayer()) {
                this.buildHandSlot = -1;
            }
            if (isInMenu()) {
                return;
            }
            if (wasKeyPressedThisTick(this.keyToggle)) {
                this.building = !this.building;
            }
            if (!this.building) {
                this.buildMark = 0;
                return;
            }

            World world = getWorld(player);

            // sets
            if (isKeyDownThisTick(this.keyA) || isKeyDownThisTick(this.keyB)) {
                int set = -1;
                for (int i = Keyboard.KEY_1; i <= Keyboard.KEY_9; i++) {
                    if (wasKeyPressedThisTick(i)) {
                        set = i - Keyboard.KEY_1;
                    }
                }
                if (set != -1) {
                    if (isKeyDownThisTick(this.keyB)) {
                        set += 9;
                    }
                    int[] data = new int[10];
                    data[0] = BUILD_ACTION_ITEMSET;
                    System.arraycopy(this.buildSets[set], 0, data, 1, 9);
                    this.serverActions.add(data);
                }
            }

            // deselect
            if (wasKeyPressedThisTick(this.keyDeselect)) {
                if (this.buildMark == 2) {
                    this.buildMark = 1;
                    this.buildSX = this.buildEX = this.buildX2 = this.buildX1;
                    this.buildSY = this.buildEY = this.buildY2 = this.buildY1;
                    this.buildSZ = this.buildEZ = this.buildZ2 = this.buildZ1;
                } else {
                    this.buildMark = 0;
                }
            }

            // action commands
            if (this.optExtension && (wasKeyPressedThisTick(this.keyMark) || wasKeyPressedThisTick(this.keyPick))) {
                // new marker position
                Entity view = getView();
                if (view == null) {
                    view = player;
                }
                int x = fix(getX(view));
                int y = fix(getY(view) + getEyeHeight(view)) - 1;
                int z = fix(getZ(view));
                if (isKeyDownThisTick(this.keyFeet)) {
                    y -= 1;
                } else if (isKeyDownThisTick(this.keyHead)) {
                    y += 1;
                } else if (wasKeyPressedThisTick(this.keyPick)) {
                    BlockFace face = getBlockFace(rayTrace(view, 128, 1f));
                    if (face != null) {
                        x = face.x;
                        y = face.y;
                        z = face.z;
                    }
                }
                // update markers
                if (this.buildMark <= 0) {
                    this.buildMark = 1;
                    this.buildSX = this.buildEX = this.buildX2 = this.buildX1 = x;
                    this.buildSY = this.buildEY = this.buildY2 = this.buildY1 = y;
                    this.buildSZ = this.buildEZ = this.buildZ2 = this.buildZ1 = z;
                } else {
                    this.buildMark = 2;
                    this.buildX2 = this.buildX1;
                    this.buildY2 = this.buildY1;
                    this.buildZ2 = this.buildZ1;
                    this.buildX1 = x;
                    this.buildY1 = y;
                    this.buildZ1 = z;
                    this.buildSX = Math.min(this.buildX1, this.buildX2);
                    this.buildSY = Math.min(this.buildY1, this.buildY2);
                    this.buildSZ = Math.min(this.buildZ1, this.buildZ2);
                    this.buildEX = Math.max(this.buildX1, this.buildX2);
                    this.buildEY = Math.max(this.buildY1, this.buildY2);
                    this.buildEZ = Math.max(this.buildZ1, this.buildZ2);
                }
            } else if (this.buildMark > 0) {
                if (wasKeyPressedThisTick(this.keyCopy)) {
                    this.buildAction(world, player, new int[]{ BUILD_ACTION_COPY, this.buildSX, this.buildSY, this.buildSZ, this.buildEX, this.buildEY, this.buildEZ });
                } else if (wasKeyPressedThisTick(this.keySet) && !isMultiplayer()) {
                    ItemStack stack = getStacks(player)[getCurrentSlot(player)];
                    int meta = getMeta(stack);
                    Block block = getBlock(stack);
                    this.serverActions.add(new int[]{ BUILD_ACTION_SET | (isKeyDownThisTick(this.keyFill) ? BUILD_MODIFIER_FILL : 0) | (isKeyDownThisTick(this.keyRemove) ? BUILD_MODIFIER_REMOVE : 0), this.buildSX, this.buildSY, this.buildSZ, this.buildEX, this.buildEY, this.buildEZ, getBlockIdMeta(getId(block), meta) });
                } else if (wasKeyPressedThisTick(this.keyPaste) && !isMultiplayer()) {
                    if (this.buildMark == 1 && !this.buildBuffer.isEmpty()) {
                        this.buildX2 = this.buildEX = this.buildSX + this.bufferWX - 1;
                        this.buildY2 = this.buildEY = this.buildSY + this.bufferWY - 1;
                        this.buildZ2 = this.buildEZ = this.buildSZ + this.bufferWZ - 1;
                        this.buildMark = 2;
                    } else if (!this.buildBuffer.isEmpty()) {
                        this.serverActions.add(new int[]{ BUILD_ACTION_PASTE | (isKeyDownThisTick(this.keyFill) ? BUILD_MODIFIER_FILL : 0) | (isKeyDownThisTick(this.keyRemove) ? BUILD_MODIFIER_REMOVE : 0), this.buildSX, this.buildSY, this.buildSZ, this.buildEX, this.buildEY, this.buildEZ });
                    }
                }
            }

            // build actions
            if (!this.clientActions.isEmpty()) {
                for (int[] action : this.clientActions) {
                    this.buildAction(world, player, action);
                }
                this.clientActions.clear();
            }
        } //synchronized
    }

    private void buildAction(@Nonnull World world, @Nonnull EntityPlayer player, int[] data) {
        final int action = data[0] & BUILD_ACTION_BITS;

        if (action == BUILD_ACTION_ITEMSET) {
            for (int slot = 0; slot < 9; slot++) {
                int idmeta = data[slot + 1];
                if (idmeta == 0 || idmeta == -1) {
                    getStacks(player)[slot] = null;
                } else {
                    getStacks(player)[slot] = getStack(idmeta, getItemMax(getItem(getBase(idmeta))));
                }
            }
            this.buildHandSlot = -1;
            return;
        }

        final boolean fill = (data[0] & BUILD_MODIFIER_FILL) != 0;
        final boolean remove = (data[0] & BUILD_MODIFIER_REMOVE) != 0;

        int sx = data[1], sy = data[2], sz = data[3];
        int ex = data[4], ey = data[5], ez = data[6];

        if (action == BUILD_ACTION_UPDATE) {
            markForUpdate(world, sx, sy, sz, ex, ey, ez);
        } else if (action == BUILD_ACTION_COPY) {
            this.bufferWX = 1 + ex - sx;
            this.bufferWY = 1 + ey - sy;
            this.bufferWZ = 1 + ez - sz;
            this.bufferSX = sx;
            this.bufferSY = sy;
            this.bufferSZ = sz;
            this.bufferEX = ex;
            this.bufferEY = ey;
            this.bufferEZ = ez;

            int size = this.bufferWX * this.bufferWY * this.bufferWZ;
            this.buildBuffer.clear();
            this.buildBufferNBT.clear();
            this.buildBuffer.ensureCapacity(size);
            this.buildBufferNBT.ensureCapacity(size);

            this.buildBufferHere = true;
            for (int x = sx; x <= ex; x++) {
                for (int y = sy; y <= ey; y++) {
                    for (int z = sz; z <= ez; z++) {
                        this.buildBuffer.add(getIdMetaAt(world, x, y, z));
                        this.buildBufferNBT.add(getTileEntityCopy(getTileEntityAt(world, x, y, z)));
                    }
                }
            }
        } else if (action == BUILD_ACTION_SET) {
            int idmeta = data[7];
            int id = getBlockId(idmeta);
            boolean sub = hasSubTypes(getItem(getBlock(id)));
            //int meta = data[8];

            if (fill) {
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
                            if (getIdAt(world, x, y, z) == 0) {
                                setIdMetaAt(world, idmeta, UPDATE_NONE, x, y, z);
                            }
                        }
                    }
                }
            } else if (remove) {
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
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
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
                            setIdMetaAt(world, idmeta, UPDATE_NONE, x, y, z);
                        }
                    }
                }
            }
            notifyAndMark(world, sx, sy, sz, ex, ey, ez);
            //clientActions.add(new int[] { BUILD_ACTION_UPDATE,
            //                  sx,sy,sz, ex,ey,ez });
        } else if (action == BUILD_ACTION_PASTE && !this.buildBuffer.isEmpty()) {
            int wx = 1 + ex - sx; wx = (wx > this.bufferWX) ? wx % this.bufferWX : 0;
            int wy = 1 + ey - sy; wy = (wy > this.bufferWY) ? wy % this.bufferWY : 0;
            int wz = 1 + ez - sz; wz = (wz > this.bufferWZ) ? wz % this.bufferWZ : 0;
            if (wx != 0 || wy != 0 || wz != 0) {
                ex -= wx;
                ey -= wy;
                ez -= wz;
                // assert : (1+ex-sx) % bufferWX == 0 || (1+ex-sx) < bufferWX
            }
            if (fill) { // fill space
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
                            if (getIdAt(world, x, y, z) == 0) {
                                int cx = (x - sx) % this.bufferWX;
                                int cy = (y - sy) % this.bufferWY;
                                int cz = (z - sz) % this.bufferWZ;
                                int at = (cx * this.bufferWY + cy) * this.bufferWZ + cz;

                                setIdMetaAt(world, this.buildBuffer.get(at), UPDATE_NONE, x, y, z);
                                if (this.buildBufferNBT.get(at) != null) {
                                    setTileEntityFromCopy(world, x, y, z, this.buildBufferNBT.get(at));
                                    setChanged(getTileEntityAt(world, x, y, z));
                                }
                            }
                        }
                    }
                }
            } else if (remove) { // remove matching
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
                            int cx = (x - sx) % this.bufferWX,
                                    cy = (y - sy) % this.bufferWY,
                                    cz = (z - sz) % this.bufferWZ;
                            int at = (cx * this.bufferWY + cy) * this.bufferWZ + cz;
                            int idmeta = this.buildBuffer.get(at), got = getIdMetaAt(world, x, y, z);
                            if (idmeta == got || (idmeta == 8 && got == 9) || (idmeta == 10 && got == 11)) {
                                setIdAt(world, 0, UPDATE_NONE, x, y, z);
                            }
                        }
                    }
                }
            } else { // replace
                for (int x = sx; x <= ex; x++) {
                    for (int y = sy; y <= ey; y++) {
                        for (int z = sz; z <= ez; z++) {
                            int cx = (x - sx) % this.bufferWX;
                            int cy = (y - sy) % this.bufferWY;
                            int cz = (z - sz) % this.bufferWZ;
                            int at = (cx * this.bufferWY + cy) * this.bufferWZ + cz;

                            setIdMetaAt(world, this.buildBuffer.get(at), UPDATE_NONE, x, y, z);
                            if (this.buildBufferNBT.get(at) != null) {
                                setTileEntityFromCopy(world, x, y, z, this.buildBufferNBT.get(at));
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
            if (!this.serverActions.isEmpty()) {
                for (int[] action : this.serverActions) {
                    try {
                        this.buildAction(world, ent, action);
                    } catch (Exception e) {
                        showOnscreenError("In Build: a build action ("+action[0]+") failed",e);
                    }
                }
                this.serverActions.clear();
            }

            // lock items in hand
            if (this.building && this.optLockQuantity) {
                ItemStack[] stacks = getStacks(ent);
                int cur = getCurrentSlot(ent);
                if (cur != this.buildHandSlot || (stacks[cur] != null && stacks[cur] != this.buildHand)) {
                    this.buildHandSlot = cur;
                    this.buildHand = stacks[cur];
                    this.buildHandSize = this.buildHand != null ? getStackSize(this.buildHand) : 0;
                } else if (stacks[cur] == null || stacks[cur] == this.buildHand) {
                    //noinspection unused
                    int size = this.buildHandSize;

                    // TODO possible bug? Did he intend to use `size` instead of `this.buildHandSize` below this part?
                    if (this.optLockQuantityRatio > 0) {
                        int max = getItemMax(getItem(this.buildHand));
                        //noinspection UnusedAssignment
                        size = (int) Math.round(Math.ceil(max * this.optLockQuantityRatio));
                    }

                    setStackSize(this.buildHand, this.buildHandSize /* `size`? */);
                    setStack(ent, cur, this.buildHand);
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
        if (this.buildMark == 2) {
            // calculate selection box
            sx = this.buildSX - x - 0.1f; ex = this.buildEX - x + 1.1f;
            sy = this.buildSY - y - 0.1f; ey = this.buildEY - y + 1.1f;
            sz = this.buildSZ - z - 0.1f; ez = this.buildEZ - z + 1.1f;
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
        if (!this.buildBuffer.isEmpty() && this.buildBufferHere) {
            sx = this.bufferSX -x - 0.06f; ex = this.bufferEX -x + 1.06f;
            sy = this.bufferSY -y - 0.06f; ey = this.bufferEY -y + 1.06f;
            sz = this.bufferSZ -z - 0.06f; ez = this.bufferEZ -z + 1.06f;
            GL11.glColor3ub((byte) 192,(byte) 192,(byte) 192);
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // draw buffer box destination
        if (!this.buildBuffer.isEmpty() && this.buildMark > 0 && !isMultiplayer()) {
            sx = this.buildSX -x - 0.08f; ex = this.buildSX + this.bufferWX -x + 0.08f;
            sy = this.buildSY -y - 0.08f; ey = this.buildSY + this.bufferWY -y + 0.08f;
            sz = this.buildSZ -z - 0.08f; ez = this.buildSZ + this.bufferWZ -z + 0.08f;
            if (this.buildEX - this.buildSX +1 == this.bufferWX
                 && this.buildEY - this.buildSY +1 == this.bufferWY
                 && this.buildEZ - this.buildSZ +1 == this.bufferWZ) {
                GL11.glColor3ub((byte) 64, (byte) 255, (byte) 64);
            } else if (this.buildMark == 1) {
                GL11.glColor3ub((byte) 192, (byte) 32, (byte) 255);
            } else {
                GL11.glColor3ub((byte) 255, (byte) 64, (byte) 64);
            }
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // draw marker 1
        if (this.buildMark > 0) {
            sx = this.buildX1 -x -0.04f; ex = this.buildX1 -x + 1.04f;
            sy = this.buildY1 -y -0.04f; ey = this.buildY1 -y + 1.04f;
            sz = this.buildZ1 -z -0.04f; ez = this.buildZ1 -z + 1.04f;
            GL11.glColor3ub((byte) 0, (byte) 255, (byte) 255);
            GuiHelper.drawLineBox(sx, sy, sz, ex, ey, ez);
        }
        // restore state
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Nullable
    @Override
    protected String getTag() {
        if (!this.building || this.tagBuild.length() == 0) {
            return null;
        }

        return this.tagBuild;
    }
}

