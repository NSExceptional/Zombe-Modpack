package zombe.core;


import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.*;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import zombe.core.content.ConfigurationScreen;
import zombe.core.content.RecipeComparator;
import zombe.core.gui.Keys;
import zombe.core.util.BlockFace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Wrapper around Minecraft objects and methods.
 *
 * The modpack and mods shall not directly call methods of Minecraft objects:
 * instead, they shall call wrapper methods provided by ZWrapper.
 *
 * This wrapper also provides access to certain private and protected
 * Minecraft methods and attributes through reflection.
 *
 * Because of all this, ZWrapper is the most version-dependent part of the
 * modpack, and may actually be the only one when Minecraft updates do not
 * break features.
 *
 * All wrappers may fail for different reasons: some methods and attributes
 * can become unavailable because of signature change (type, arguments),
 * name change, visibility change or a missing file.
 * Mod conflicts and Minecraft updates will often cause such failures.
 */
public class ZWrapper {

    //-EntityList-------------------------------------------------------------
    // entity type ids, taken from EntityList
    @Deprecated public static final int ITEM = 1, XPORB = 2, LEASHKNOT = 8, PAINTING = 9, ARROW = 10, SNOWBALL = 11, FIREBALL = 12, SMALLFIREBALL = 13, THROWNENDERPEARL = 14, EYEOFENDERSIGNAL = 15, THROWNPOTION = 16, THROWNEXPBOTTLE = 17, ITEMFRAME = 18, WITHERSKULL = 19, PRIMEDTNT = 20, FALLINGSAND = 21, FIREWORKSROCKETENTITY = 22, ARMORSTAND = 30, MINECARTCOMMANDBLOCK = 40, BOAT = 41, MINECART = 42, MINECARTCHEST = 43, MINECARTFURNACE = 44, MINECARTTNT = 45, MINECARTHOPPER = 46, MINECARTSPAWNER = 47, MOB = 48, LIVING = 48, MONSTER = 49, CREEPER = 50, SKELETON = 51, SKELLY = 51, SPIDER = 52, GIANT = 53, ZOMBIE = 54, SLIME = 55, GHAST = 56, PIGZOMBIE = 57, ENDERMAN = 58, CAVESPIDER = 59, SILVERFISH = 60, BLAZE = 61, LAVASLIME = 62, ENDERDRAGON = 63, DRAGON = 63, WITHERBOSS = 64, BAT = 65, WITCH = 66, ENDERMITE = 67, GUARDIAN = 68, PIG = 90, SHEEP = 91, COW = 92, CHICKEN = 93, SQUID = 94, WOLF = 95, MUSHROOMCOW = 96, REDCOW = 96, SNOWMAN = 97, OZELOT = 98, OCELOT = 98, VILLAGERGOLEM = 99, GOLEM = 99, ENTITYHORSE = 100, HORSE = 100, RABBIT = 101, VILLAGER = 120, ENDERCRYSTAL = 200, PLAYER = 3, // dummy id, not in the actual entity list
            ENTITY_MAX = 201, MAXTYPE = 201;

    /* REFLECTION STUFF */
    public static final int UPDATE_NONE = 0, NOTIFY_SERVER = 1, MARK_SERVER = 2, NOTIFY_MARK_SERVER = 3, MARK_BOTH = 6, NOTIFY_MARK_ALL = 7;
    public static final int ID_BASE = 0xffff, ID_META = 0x7fff, ID_ANY = 0x7fff, ID_ONE = 0x3fff, BLOCK_BASE = 0xfff, BLOCK_META = 0xf, BLOCK_ANY = 0xf, BLOCK_ONE = 0x7;
    private static final Logger logger = Logger.getLogger("zombe.core");
    /* table of MCP / obfuscated attribute names correspondance */
    private static final String[] MCPnames = {
        // EntityMinecart
        "em_fuel",                  "fuel",                     "c",
        // EntityEnderman
        "ee_canCarryBlocks",        "carriableBlocks",          "br",
        // TileEntityFurnace
        "tef_furnaceItemStacks",    "furnaceItemStacks",        "g",
        // TileEntityChest
        "tec_chestContents",        "chestContents",            "i",
        // TileEntityDispenser
        "ted_stacks",               "stacks",                   "i",
        // GuiNewChat
        "gnc_ChatLines",            "chatLines",                "c",
        // Block
        "b_blockHardness",          "blockHardness",            "v",
        "b_blockResistance",        "blockResistance",          "cH",
        // ItemStack
        "is_stackSize",             "stackSize",                ""
    };
    private static final List<String> MCPNamesList = ImmutableList.copyOf(MCPnames);
    @Nullable public static Field fBlockHardness = getField(Block.class, "b_blockHardness");
    @Nullable public static Field fBlockResist = getField(Block.class, "b_blockResistance");
    // note: used to be typeName[]
    @Deprecated private static final String entityNames[] = {
            // 0
            "???", "Item", "XPOrb", "Player", null, null, null, null, "LeashKnot", "Painting",
            // 10
            "Arrow", "Snowball", "Fireball", "SmallFireball", "ThrownEnderpearl", "EyeOfEnderSignal", "ThrownPotion", "ThrownExpBottle", "ItemFrame", "WitherSkull",
            // 20
            "PrimedTnt", "FallingSand", "FireworksRocketEntity", null, null, null, null, null, null, null,
            // 30
            "ArmorStand", null, null, null, null, null, null, null, null, null,
            // 40
            "MinecartCommandBlock", "Boat", "Minecart", "MinecartChest", "MinecartFurnace", "MinecartTnt", "MinecartHopper", "MinecartSpawner", "Mob", "Monster",
            // 50
            "Creeper", "Skeleton", "Spider", "Giant", "Zombie", "Slime", "Ghast", "PigZombie", "Enderman", "CaveSpider",
            // 60
            "Silverfish", "Blaze", "LavaSlime", "EnderDragon", "WitherBoss", "Bat", "Witch", "Endermite", "Guardian", null,
            // 70
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            // 90
            "Pig", "Sheep", "Cow", "Chicken", "Squid", "Wolf", "MushroomCow", "SnowMan", "Ozelot", "VillagerGolem", "EntityHorse", "Rabbit", null, null, null, null, null, null, null, null,
            // 100
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            // 120
            "Villager", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            // 200
            "EnderCrystal" };
    private static final double FINE_FACING_FACTOR = Math.tan(Math.PI * 0.125);
    private static boolean exceptionReported = false;
    //-GuiNewChat-------------------------------------------------------------
    @Nullable private static Field fChatLines = getField(GuiNewChat.class, "gnc_ChatLines");
    @Nullable private static Field fStackSize = getField(ItemStack.class, "is_stackSize");
    //-TileEntityChest--------------------------------------------------------
    @Nullable private static Field fChestItems = getField(TileEntityChest.class, "tec_chestContents");
    //-TileEntityDispenser----------------------------------------------------
    @Nullable private static Field fDispItems = getField(TileEntityDispenser.class, "ted_stacks");
    //-TileEntityFurnace------------------------------------------------------
    @Nullable private static Field fFurnaceItems = getField(TileEntityFurnace.class, "tef_furnaceItemStacks");

    private static void log(@Nonnull String text, Exception e) {
        ZMod.log(text, e);
    }

    private static void log(@Nonnull String text) {
        ZMod.log(text, null);
    }

    private static void err(String text, Exception e) {
        ZMod.showOnscreenError(text, e);
    }

    private static void err(String text) {
        ZMod.showOnscreenError(text, null);
    }

    private static void reportException(Exception error) {
        if (exceptionReported) {
            return;
        }
        exceptionReported = true;
        err("exception in reflection code encountered !", error);
    }

    public static boolean classExists(String name) {
        try {
            if (Class.forName(name) != null) {
                return true;
            }
        } catch (Exception whatever) {
        }
        return false;
    }

    @Nullable
    public static Field getField(@Nonnull Class c, String name) {
        int index = MCPNamesList.indexOf(name);
        if (index == -1) {
            log("getField failed for: " + name);
            return null;
        }
        Field field;
        try {
            field = c.getDeclaredField(MCPnames[index + 1]);
        } catch (Exception whatever) {
            try {
                field = c.getDeclaredField(MCPnames[index + 2]);
            } catch (Exception error) {
                log("exception in reflection code encountered ! (missing field: '" + MCPnames[index + 1] + "', obfus: '" + MCPnames[index + 2] + "')", error);
                return null;
            }
        }
        try {
            field.setAccessible(true);
        } catch (Exception error) {
            log("exception in reflection code encountered ! (field not accessible: '" + MCPnames[index + 1] + "', obfus: '" + MCPnames[index + 2] + "')", error);
            return null;
        }
        return field;
    }


    /* ACCESS METHODS */

    @Nullable
    public static Object getValue(@Nonnull Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception error) {
            reportException(error);
        }
        return null;
    }

    public static void setValue(@Nonnull Field field, Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (Exception error) {
            reportException(error);
        }
    }

    @Nullable
    public static Class getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception error) {
            reportException(error);
        }
        return null;
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj, Object param[]) {
        try {
            return m.invoke(obj, param);
        } catch (Exception error) {
            reportException(error);
        }
        return null;
    }

    @Nullable
    public static Object getResult(@Nonnull Method m) {
        return getResult(m, null, new Object[]{});
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj) {
        return getResult(m, obj, new Object[]{});
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj, Object p1) {
        return getResult(m, obj, new Object[]{ p1 });
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj, Object p1, Object p2) {
        return getResult(m, obj, new Object[]{ p1, p2 });
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj, Object p1, Object p2, Object p3) {
        return getResult(m, obj, new Object[]{ p1, p2, p3 });
    }

    @Nullable
    public static Object getResult(@Nonnull Method m, Object obj, Object p1, Object p2, Object p3, Object p4) {
        return getResult(m, obj, new Object[]{ p1, p2, p3, p4 });
    }

    private static boolean checkClass(@Nonnull Class c) {
        return checkClass("zmodmarker", c);
    }

    private static boolean checkClass(@Nonnull String name, @Nonnull Class c) {
        try {
            Field field = c.getDeclaredField(name);
            if (field != null) {
                return true;
            }
        } catch (Exception whatever) {
        }
        return false;
    }

    //-Biome------------------------------------------------------------------
    @Nonnull
    public static String getBiomeName(int x, int z) {
        BiomeProvider provider = getWorld().getBiomeProvider();
        return getChunkFromBlockCoords(x, z).getBiome(new BlockPos(x, 0, z), provider).getBiomeName();
    }

    //-Block------------------------------------------------------------------
    @Nullable
    public static Block getBlock(@Nullable IBlockState state) {
        return state == null ? null : state.getBlock();
    }

    @Nonnull
    public static Block getBlock(ItemStack stack) {
        return getBlock(getItem(stack));
    }

    @Nonnull
    public static Block getBlock(Item item) {
        return Block.getBlockFromItem(item);
    }

    @Nullable
    public static Block getBlock(@Nonnull String name) {
        return Block.getBlockFromName(name);
    }

    @Nonnull
    @Deprecated
    public static Block getBlock(int id) {
        //return Block.blocksList[id]; // < 1.8
        return Block.getBlockById(id);
    }

    @Nullable
    public static String getName(@Nullable Block block) {
        return block == null ? null : block.getUnlocalizedName();
    }

    public static void setBlock(int id, Block val) {
        //Block.blocksList[id] = val; // < 1.8
        //TODO: look into Block.blockRegistry
    }

    public static int getId(IBlockState state) {
        return getId(getBlock(state));
    }

    @Deprecated
    public static int getId(@Nonnull Block block) {
        return Block.getIdFromBlock(block);
    }

    public static int getMeta(@Nullable IBlockState state) {
        return state == null ? 0 : getBlock(state).getMetaFromState(state);
    }

    public static int getIdMeta(IBlockState state) {
        int idmeta = getBlockIdMeta(state);
        return getIdMeta(getBlockId(idmeta), getBlockMeta(idmeta));
    }

    public static int getBlockIdMeta(@Nullable IBlockState state) {
        return state == null ? 0 : Block.getStateId(state);
    }

    public static int getBlockIdMeta(int id, int meta) {
        return (((meta < 0 || meta == ID_ANY || meta == BLOCK_ANY) ? BLOCK_ANY : meta & BLOCK_ONE) << 12) | (id & BLOCK_BASE);
    }

    public static int getBlockIdMeta(int idmeta) {
        return getBlockIdMeta(getBase(idmeta), getMeta(idmeta));
    }

    public static int getBlockIdMeta(ItemStack stack) {
        return getBlockIdMeta(getState(stack));
    }

    public static int getBlockId(int idmeta) {
        return idmeta & BLOCK_BASE;
    }

    public static int getBlockMeta(int idmeta) {
        return (idmeta >> 12) & BLOCK_META;
    }

    public static int getBlockMeta(@Nullable ItemStack stack) {
        return stack == null ? 0 : getItem(stack).getMetadata(getMeta(stack));
    }

    @Nonnull
    @Deprecated
    public static IBlockState getState(int idmeta) {
        return Block.getStateById(idmeta);
    }

    @Nullable
    public static IBlockState getState(ItemStack stack) {
        return getState(getBlock(stack), getMeta(stack));
    }

    @Nullable
    @Deprecated
    public static IBlockState getState(@Nullable Block block, int meta) {
        return block == null ? null : block.getStateFromMeta(meta);
    }

    @Nullable
    @Deprecated
    public static IBlockState getState(int id, int meta) {
        return getState(getBlock(id), meta);
    }

    @Nullable
    public static IBlockState getDefaultState(@Nullable Block block) {
        return block == null ? null : block.getDefaultState();
    }

    @Nullable
    @Deprecated
    public static IBlockState getDefaultState(int id) {
        return getDefaultState(getBlock(id));
    }

    @Nonnull
    @Deprecated
    public static Material getBlockMaterial(@Nonnull Block block) {
        //return block.blockMaterial; // < 1.8
        return block.getMaterial(null);
    }

    @Nonnull
    public static Material getBlockMaterial(int id) {
        return getBlockMaterial(getBlock(id));
    }

    @Deprecated
    public static float getBlockHardness(@Nonnull Block block) {
        //return (Float)getValue(fBlockHardness, block);
        return block.getBlockHardness(null, null, null);
    }

    public static void setBlockHardness(Block block, float val) {
        setValue(fBlockHardness, block, val);
    }

    public static float getBlockResist(@Nonnull Block block) {
        //return (Float)getValue(fBlockResist, block);
        return block.getExplosionResistance(null) * 5;
    }

    public static void setBlockResist(Block block, float val) {
        setValue(fBlockResist, block, val);
    }

    public static float getBlockSlip(@Nonnull Block block) {
        return block.slipperiness;
    }

    public static void setBlockSlip(@Nonnull Block block, float val) {
        block.slipperiness = val;
    }

    @Deprecated
    public static boolean getBlockIsOpaque(@Nullable Block block) {
        return block != null && block.isOpaqueCube(null);
    }

    @Deprecated
    public static boolean getBlockIsOpaque(int id) {
        //return Block.opaqueCubeLookup[id]; // < 1.8
        return getBlockIsOpaque(getBlock(id));
    }

    @Deprecated
    public static int getBlockOpacity(@Nullable Block block) {
        return block == null ? 0 : block.getLightOpacity(null);
    }

    @Deprecated
    public static int getBlockOpacity(int id) {
        //return Block.lightOpacity[id]; // < 1.8
        return getBlockOpacity(getBlock(id));
    }

    public static void setBlockOpacity(int id, int val) {
        //Block.lightOpacity[id] = val; // < 1.8
    }

    @Deprecated
    public static int getBlockLight(@Nullable Block block) {
        return block == null ? 0 : block.getLightValue(null);
    }

    @Deprecated
    public static int getBlockLight(int id) {
        //return Block.lightValue[id]; // < 1.8
        return getBlockLight(getBlock(id));
    }

    //-ChunkCoordinates-------------------------------------------------------
    /* obsolete
    public static int getX(ChunkCoordinates pos) { return pos.posX; }
    public static int getY(ChunkCoordinates pos) { return pos.posY; }
    public static int getZ(ChunkCoordinates pos) { return pos.posZ; }
    */

    public static void setBlockLight(int id, int val) {
        //Block.lightValue[id] = val; // < 1.8
    }

    @Deprecated
    public static boolean getBlockIsFull(@Nullable Block block) {
        //return block != null && block.renderAsNormalBlock(); // < 1.8 MCP 9.10
        return block != null && block.isFullCube(null);
    }

    @Deprecated
    public static boolean getBlockIsFull(int id) {
        return getBlockIsFull(getBlock(id));
    }

    @Deprecated
    public static boolean getBlockIsSpawn(int id) {
        return getBlock(id) != null && getBlockMaterial(id).isOpaque() && getBlockIsFull(id);
    }

    public static void setBlockGraphicsLevel(Block block, boolean flag) {
        //((BlockLeaves)block).setGraphicsLevel(flag);
    }

    //-BlockFire--------------------------------------------------------------
    public static int getFireSpread(int id) {
        return Blocks.FIRE.getEncouragement(getBlock(id));
    }

    public static int getFireBurn(int id) {
        return Blocks.FIRE.getFlammability(getBlock(id));
    }

    //-ChatLine---------------------------------------------------------------
    @Nullable
    public static ChatLine getChatLine(@Nullable List<ChatLine> lines, int line) {
        return lines == null ? null : lines.get(line);
    }

    @Nullable
    public static String getChatText(@Nullable ChatLine line) {
        return line == null ? null : line.getChatComponent().getUnformattedText();
    }

    @Nullable
    public static String getChatText(List<ChatLine> lines, int line) {
        return getChatText(getChatLine(lines, line));
    }

    public static void sortRecipes(@Nonnull List<IRecipe> recipes) {
        //Collections.sort(recipes, new RecipeSorter(getCManager())); // < 1.8
        Collections.sort(recipes, new RecipeComparator());
    }

    //-Entity-----------------------------------------------------------------
    public static boolean getNoclip(@Nonnull Entity ent) {
        return ent.noClip;
    }

    public static void setNoclip(@Nonnull Entity ent, boolean val) {
        if (!isMultiplayer() || ent != getPlayer()) {
            ent.noClip = val;
        }
    }

    @Nullable
    public static RayTraceResult rayTrace(@Nonnull Entity ent, double dist, float f) {
        return ent.rayTrace(dist, f);
    }

    @Nullable
    public static RayTraceResult rayTrace(double dist, float f) {
        return rayTrace(getView(), dist, f);
    }

    public static boolean rayTraceHit(@Nonnull Entity ent, double dist, float f) {
        return rayTrace(ent, dist, f) != null;
    }

    public static boolean rayTraceHit(double dist, float f) {
        return rayTraceHit(getView(), dist, f);
    }

    @Nonnull
    public static Vec3d getLookVector(@Nonnull Entity ent, float delta) {
        return ent.getLook(delta);
    }

    @Nonnull
    public static World getWorld(@Nonnull Entity ent) {
        return ent.getEntityWorld(); // > 1.8 ?
    }

    public static double getYOffset(@Nonnull Entity ent) {
        //return ent.yOffset; // < 1.8
        return ent.getYOffset();
    }

    public static double getYFix(@Nonnull Entity ent) {
        return getAABB(ent).minY - getY(ent);
    }

    @Nonnull
    public static BlockPos getPos(@Nonnull Entity ent) {
        return ent.getPosition();
    }

    @Nonnull
    public static Vec3d getPosition(@Nonnull Entity ent) {
        return ent.getPositionVector();
    }

    @Nonnull
    public static Vec3d getPositionDelta(@Nonnull Entity ent, float delta) {
        double x = getX(ent), px = getPrevX(ent);
        double y = getY(ent), py = getPrevY(ent);
        double z = getZ(ent), pz = getPrevZ(ent);
        return new Vec3d(px + (x - px) * delta, py + (y - py) * delta, pz + (z - pz) * delta);
    }

    public static void setPos(@Nonnull Entity ent, @Nonnull BlockPos pos) {
        ent.setPosition(getX(pos), getY(pos), getZ(pos));
    }

    public static void setPosition(@Nonnull Entity ent, @Nonnull Vec3d vec) {
        ent.setPosition(getX(vec), getY(vec), getZ(vec));
    }

    public static void setPosition(@Nonnull Entity ent, double x, double y, double z) {
        ent.setPosition(x, y, z);
    }

    @Nonnull
    public static AxisAlignedBB getBoundingBox(@Nonnull Entity ent) {
        //return ent.boundingBox; // < 1.8
        return ent.getEntityBoundingBox();
    }

    @Nonnull
    public static AxisAlignedBB getAABB(@Nonnull Entity ent) {
        return getBoundingBox(ent);
    }

    public static void setBoundingBox(@Nonnull Entity ent, @Nonnull AxisAlignedBB box) {
        //ent.boundingBox = box; // < 1.8
        ent.setEntityBoundingBox(box);
    }

    public static void setAABB(@Nonnull Entity ent, @Nonnull AxisAlignedBB box) {
        setBoundingBox(ent, box);
    }

    public static float getWidth(@Nonnull Entity ent) {
        return ent.width;
    }

    public static float getHeight(@Nonnull Entity ent) {
        return ent.height;
    }

    public static float getEyeHeight(@Nonnull Entity ent) {
        return ent.getEyeHeight();
    }

    public static float getYaw(@Nonnull Entity ent) {
        return ent.rotationYaw;
    }

    public static void setYaw(@Nonnull Entity ent, float yaw) {
        ent.rotationYaw = yaw;
    }

    public static float getPitch(@Nonnull Entity ent) {
        return ent.rotationPitch;
    }

    public static void setPitch(@Nonnull Entity ent, float pitch) {
        ent.rotationPitch = pitch;
    }

    @Nullable
    public static Entity getOnEntity(@Nonnull Entity ent) {
        return ent.getRidingEntity();
    }

    public static double getMountOffset(@Nonnull Entity ent) {
        return ent.getMountedYOffset();
    }

    public static float getSteps(@Nonnull Entity ent) {
        return ent.distanceWalkedModified;
    }

    public static void setSteps(@Nonnull Entity ent, float val) {
        ent.distanceWalkedModified = val;
    }

    public static void dieEntity(@Nonnull Entity ent) {
        ent.setDead();
    }

    public static boolean getOnGround(@Nonnull Entity ent) {
        return ent.onGround;
    }

    public static void setOnGround(@Nonnull Entity ent, boolean val) {
        ent.onGround = val;
    }

    @Nonnull
    public static Vec3d getMotion(@Nonnull Entity ent) {
        return new Vec3d(getMotionX(ent), getMotionY(ent), getMotionZ(ent));
    }

    public static void setMotion(@Nonnull Entity ent, double x, double y, double z) {
        ent.setVelocity(x, y, z);
    }

    public static void setMotion(@Nonnull Entity ent, @Nonnull Vec3d motion) {
        setMotion(ent, getX(motion), getY(motion), getZ(motion));
    }

    public static double getMotionX(@Nonnull Entity ent) {
        return ent.motionX;
    }

    public static void setMotionX(@Nonnull Entity ent, double val) {
        ent.motionX = val;
    }

    public static double getMotionY(@Nonnull Entity ent) {
        return ent.motionY;
    }

    public static void setMotionY(@Nonnull Entity ent, double val) {
        ent.motionY = val;
    }

    public static double getMotionZ(@Nonnull Entity ent) {
        return ent.motionZ;
    }

    public static void setMotionZ(@Nonnull Entity ent, double val) {
        ent.motionZ = val;
    }

    public static double getX(@Nonnull Entity ent) {
        return ent.posX;
    }

    public static void setX(@Nonnull Entity ent, double val) {
        ent.posX = val;
    }

    public static double getY(@Nonnull Entity ent) {
        return ent.posY;
    }

    public static void setY(@Nonnull Entity ent, double val) {
        ent.posX = val;
    }

    public static double getZ(@Nonnull Entity ent) {
        return ent.posZ;
    }

    public static void setZ(@Nonnull Entity ent, double val) {
        ent.posX = val;
    }

    public static double getPrevX(@Nonnull Entity ent) {
        return ent.prevPosX;
    }

    public static void setPrevX(@Nonnull Entity ent, double val) {
        ent.prevPosX = val;
    }

    public static double getPrevY(@Nonnull Entity ent) {
        return ent.prevPosY;
    }

    public static void setPrevY(@Nonnull Entity ent, double val) {
        ent.prevPosY = val;
    }

    public static double getPrevZ(@Nonnull Entity ent) {
        return ent.prevPosZ;
    }

    public static void setPrevZ(@Nonnull Entity ent, double val) {
        ent.prevPosZ = val;
    }

    public static double getLastX(@Nonnull Entity ent) {
        return ent.lastTickPosX;
    }

    public static void setLastX(@Nonnull Entity ent, double val) {
        ent.lastTickPosX = val;
    }

    public static double getLastY(@Nonnull Entity ent) {
        return ent.lastTickPosY;
    }

    public static void setLastY(@Nonnull Entity ent, double val) {
        ent.lastTickPosY = val;
    }

    public static double getLastZ(@Nonnull Entity ent) {
        return ent.lastTickPosZ;
    }

    public static void setLastZ(@Nonnull Entity ent, double val) {
        ent.lastTickPosZ = val;
    }

    public static void setFire0(@Nonnull Entity ent) {
        ent.extinguish();
    }

    public static void setAir(@Nonnull Entity ent, int val) {
        ent.setAir(val);
    }

    public static void setFall(@Nonnull Entity ent, float val) {
        ent.fallDistance = val;
    }

    // deprecated stuff

    public static int getAge(@Nonnull Entity ent) {
        return ent.ticksExisted;
    }

    public static void setAge(@Nonnull Entity ent, int val) {
        ent.ticksExisted = val;
    }

    public static void setFireImmune(Entity ent, boolean immune) {
        //ent.isImmuneToFire = immune; // < 1.8
    }

    public static void setPositions(@Nonnull Entity ent, double x, double y, double z) {
        ent.setPosition(0.0D, 0.0D, 0.0D);
        ent.prevPosX = ent.lastTickPosX = ent.posX = x;
        ent.prevPosY = ent.lastTickPosY = ent.posY = y;
        ent.prevPosZ = ent.lastTickPosZ = ent.posZ = z;
        ent.setPosition(x, y, z);
    }

    @Deprecated
    public static int getId(Entity ent) {
        if (ent instanceof EntityPlayer) {
            return PLAYER;
        }
        return ent.getEntityId();
    }

    @Deprecated
    public static int getEntityId(@Nonnull String type) {
        if (type.equals("???")) {
            return 0;
        }
        if (type.equals("Player")) {
            return PLAYER;
        }
        if (type.equals("Pig")) {
            return PIG; // bugfix for func_180122_a()
        }
        int id = EntityList.getEntityIDFromString(type);
        return (id == PIG) ? 0 : id;      // bugfix for func_180122_a()
    }

    @Nonnull
    public static String getType(Entity ent) {
        if (ent instanceof EntityPlayer) {
            return "Player";
        }
        String type = EntityList.getEntityString(ent);
        return (type == null) ? "???" : type;
    }

    @Nonnull
    @Deprecated
    public static String getEntityType(int id) {
        if (id == PLAYER) {
            return "Player";
        }
        String type = EntityList.getEntityStringFromID(id);
        return (type == null) ? "???" : type;
    }

    @Nonnull
    public static String getName(Entity ent) {
        if (ent instanceof EntityPlayer) {
            return getPlayerName((EntityPlayer) ent);
        }
        String type = EntityList.getEntityString(ent);
        return (type == null) ? "???" : type;
    }

    //-EntityLivingBase-------------------------------------------------------
    public static void setHealth(@Nonnull EntityLivingBase ent, float val) {
        ent.setHealth(val);
    }

    public static float getHealth(@Nonnull EntityLivingBase ent) {
        return ent.getHealth();
    }

    public static float getMaxHealth(@Nonnull EntityLivingBase ent) {
        return ent.getMaxHealth();
    }

    public static void setEntitySize(@Nonnull EntityLivingBase ent, float height, float health) {
        ent.width *= height / ent.height;
        ent.height = height;
        setHealth(ent, health);
        ent.setPosition(ent.posX, ent.posY, ent.posZ);
    }

    //-EntityPlayer-----------------------------------------------------------
    @Nonnull
    public static String getPlayerName(@Nonnull EntityPlayer ent) {
        //return ent.username;
        return ent.getName();
    }

    @Nonnull
    public static GameProfile getProfile(@Nonnull EntityPlayer ent) {
        return ent.getGameProfile();
    }

    @Nullable
    public static BlockPos getSpawn(EntityPlayer ent) {
        //return ent.getBedLocation();
        return null;
    }

    public static BlockPos getBed(@Nonnull EntityPlayer ent) {
        return ent.bedLocation;
    }

    public static boolean isServerPlayer(EntityPlayer e) {
        EntityPlayerSP player = getPlayer();
        return player != null && e instanceof EntityPlayerMP && getPlayerName(e).equals(getPlayerName(player));
    }

    public static boolean isCreative(@Nullable EntityPlayer ent) {
        return ent != null && ent.capabilities.isCreativeMode;
    }

    public static boolean isSleeping(@Nonnull EntityPlayer ent) {
        return ent.isPlayerSleeping();
    }

    @Deprecated
    public static boolean getSleeping(@Nonnull EntityPlayer ent) {
        return ent.isPlayerSleeping();
    }

    public static boolean getFlying(@Nonnull EntityPlayer ent) {
        return ent.capabilities.isFlying;
    }

    public static void setFlying(@Nonnull EntityPlayer ent, boolean val) {
        ent.capabilities.isFlying = val;
    }

    //-EntityPlayerSP---------------------------------------------------------
    public static void chatClient(@Nonnull String str) {
        addChatMessage(new TextComponentString(str));
    }

    public static void addChatMessage(@Nonnull ITextComponent message) {
        getPlayer().addChatMessage(message);
    }

    public static void sendChat(@Nonnull String message) {
        getPlayer().sendChatMessage(message);
    }

    public static boolean getNoclip() {
        return getNoclip(getPlayer());
    }

    //-EntityMinecart---------------------------------------------------------
    //private static Field fCartFuel = getField(EntityMinecart.class, "em_fuel");
    //private static int getCartType(EntityMinecart ent) { return ent.minecartType; }
    //private static int getCartFuel(EntityMinecart ent) { return (Integer)getValue(fCartFuel, ent); }
    //private static void setCartFuel(EntityMinecart ent, int val) { setValue(fCartFuel, ent, val); }

    public static void setNoclip(boolean val) {
        setNoclip(getPlayer(), val);
    }

    public static boolean getFlying() {
        return getFlying(getPlayer());
    }

    public static void setFlying(boolean val) {
        setFlying(getPlayer(), val);
    }

    public static float getDefaultReach() {
        return isCreative(getPlayer()) ? 5.0F : 4.5F;
    }

    public static void sendMotionUpdates(@Nonnull EntityPlayerSP player) {
        //player.sendMotionUpdates(); // < 1.8
        //player.func_175161_p(); // MCP 9.10
        player.onUpdateWalkingPlayer();
    }

    //-EntityItem-------------------------------------------------------------
    @Nonnull
    private static ItemStack getEntityItemStack(@Nonnull EntityItem ent) {
        return ent.getEntityItem();
    }

    //-EnumFacing-------------------------------------------------------------
    public static int getFacing(@Nullable EnumFacing facing) {
        return facing == null ? -1 : facing.getIndex();
    }

    @Nonnull
    public static EnumFacing getFacing(int side) {
        return EnumFacing.getFront(side);
    }

    @Nonnull
    public static Vec3i getDirectionVec(@Nonnull EnumFacing facing) {
        return facing.getDirectionVec();
    }

    @Nonnull
    @Deprecated
    public static Vec3i getDirectionVec(int facing) {
        return getDirectionVec(getFacing(facing));
    }

    @Nonnull
    public static String getName(@Nonnull EnumFacing facing) {
        return facing.getName();
    }

    @Nonnull
    @Deprecated
    public static String getFacingName(int facing) {
        return getName(getFacing(facing));
    }

    @Nonnull
    public static String getShortName(@Nonnull EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return "D";
            case UP:
                return "U";
            case NORTH:
                return "N";
            case SOUTH:
                return "S";
            case WEST:
                return "W";
            case EAST:
                return "E";
        }
        return "";
    }

    @Nonnull
    @Deprecated
    public static String getFacingShortName(int facing) {
        return getShortName(getFacing(facing));
    }

    @Nonnull
    public static EnumFacing getFacing(double dx, double dy, double dz) {
        EnumFacing facing = EnumFacing.NORTH;
        double colinearity = 0;
        for (EnumFacing f : EnumFacing.values()) {
            Vec3i dir = getDirectionVec(f);
            double c = getX(dir) * dx + getY(dir) * dy + getZ(dir) * dz;
            if (c > colinearity) {
                facing = f;
                colinearity = c;
            }
        }
        return facing;
    }

    @Nonnull
    public static String getFineFacingName(double dx, double dy, double dz) {
        String main = getShortName(getFacing(dx, dy, dz));
        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        if (ax > ay && ax > az) {
            dx *= FINE_FACING_FACTOR;
        }
        if (ay > ax && ay > az) {
            dy *= FINE_FACING_FACTOR;
        }
        if (az > ax && az > ay) {
            dz *= FINE_FACING_FACTOR;
        }
        String fine = getShortName(getFacing(dx, dy, dz));
        if (!main.equals(fine)) {
            if (fine.equals("N") || fine.equals("S") || main.equals("U") || main.equals("D")) {
                main = fine + main;
            } else {
                main += fine;
            }
        }
        return main;
    }

    @Nonnull
    public static String getRelativeCompass(double dx, double dy, double x, double y) {
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) {
            len = 1;
        }
        dx /= len;
        dy /= len;
        double rx = dx * x + dy * y;
        double ry = -dy * x + dx * y;
        x = Math.abs(rx);
        y = Math.abs(ry);
        if (rx > y) {
            return "^";
        }
        if (rx < -y) {
            return "v";
        }
        if (ry > x) {
            return "<";
        }
        if (ry < -x) {
            return ">";
        }
        return "o";
    }

    //-FontRenderer-----------------------------------------------------------
    public static FontRenderer getFontRenderer() {
        //return getMinecraft().fontRenderer; < MCP 9
        return getMinecraft().fontRendererObj;
    }

    public static void drawString(@Nonnull String str, int x, int y, int color) {
        getFontRenderer().drawString(str, x, y, color);
    }

    public static void drawStringWithShadow(@Nonnull String str, int x, int y, int color) {
        getFontRenderer().drawStringWithShadow(str, x, y, color);
    }

    public static int getStringWidth(@Nonnull String str) {
        return getFontRenderer().getStringWidth(str);
    }

    public static int getCharWidth(char c) {
        return getFontRenderer().getCharWidth(c);
    }

    @Nonnull
    public static String trimStringToWidth(@Nonnull String str, int width) {
        return getFontRenderer().trimStringToWidth(str, width);
    }

    //-GameSettings-----------------------------------------------------------
    public static boolean isHideGUI() {
        return getGameSettings().hideGUI;
    }

    public static boolean isShowDebug() {
        return getGameSettings().showDebugInfo;
    }

    public static int getKeyCode(@Nonnull KeyBinding kb) {
        //return kb.keyCode; // < 1.8
        return kb.getKeyCode();
    }

    public static int getKeyJump() {
        return getKeyCode(getGameSettings().keyBindJump);
    }

    public static int getKeyGo() {
        return getKeyCode(getGameSettings().keyBindForward);
    }

    public static int getKeyBack() {
        return getKeyCode(getGameSettings().keyBindBack);
    }

    public static int getViewDistance() {
        //return getGameSettings().renderDistance;
        return getGameSettings().renderDistanceChunks; //?
    }

    public static float getGamma() {
        return getGameSettings().gammaSetting;
    }

    public static void setGamma(float gamma) {
        getGameSettings().gammaSetting = gamma;
    }

    //-Gui--------------------------------------------------------------------
    public static void drawRect(int l, int t, int r, int b, int color) {
        Gui.drawRect(l, t, r, b, color);
    }

    public static void drawGradientRect(int l, int t, int r, int b, int c1, int c2) {
        //Gui.drawGradientRect(l,t,r,b, c1,c2); // this one isn't static...
        drawRect(l, t, r, b, c1);
    }

    //-GuiAchievement---------------------------------------------------------
    public static GuiAchievement getAchievementGUI() {
        return getMinecraft().guiAchievement;
    }

    public static void killAchievement() {
        //setValue(fAchivement, getMinecraft().guiAchievement, null);
        getAchievementGUI().clearAchievements();
    }

    //-GuiIngame--------------------------------------------------------------
    public static GuiIngame getIngameGUI() {
        return getMinecraft().ingameGUI;
    }

    @Nonnull
    public static GuiNewChat getChatGUI() {
        return getIngameGUI().getChatGUI();
    }

    @Nullable
    public static List<ChatLine> getChatLines() {
        if (fChatLines == null) {
            return null;
        }
        return (List<ChatLine>) getValue(fChatLines, getChatGUI());
    }

    public static void printChatMessage(@Nonnull String str) {
        printChatMessage(new TextComponentString(str));
    }

    public static void printChatMessage(@Nonnull ITextComponent message) {
        getChatGUI().printChatMessage(message);
    }

    public static boolean isRecipeMatch(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting grid) {
        return recipe.matches(grid, getWorld());
    }

    //-InventoryPlayer--------------------------------------------------------
    @Nonnull
    public static ItemStack[] toISArray(@Nonnull List l) {
        ItemStack[] buf = new ItemStack[l.size()];
        return (ItemStack[]) l.toArray(buf);
    }

    public static InventoryPlayer getInventory(@Nonnull EntityPlayer ent) {
        return ent.inventory;
    }

    @Nonnull
    public static ItemStack[] getStacks(@Nonnull InventoryPlayer inv) {
        return toISArray(inv.mainInventory);
    }

    @Nonnull
    public static ItemStack[] getStacks(@Nonnull EntityPlayer ent) {
        return getStacks(getInventory(ent));
    }

    @Nonnull
    public static ItemStack[] getArmors(@Nonnull InventoryPlayer inv) {
        return toISArray(inv.armorInventory);
    }

    @Nonnull
    public static ItemStack[] getArmors(@Nonnull EntityPlayer ent) {
        return getArmors(getInventory(ent));
    }

    public static void setStack(@Nonnull InventoryPlayer inv, int loc, ItemStack stack) {
        getStacks(inv)[loc] = stack;
    }

    public static void setStack(@Nonnull EntityPlayer ent, int loc, ItemStack stack) {
        setStack(getInventory(ent), loc, stack);
    }

    public static int getCurrentSlot(@Nonnull InventoryPlayer inv) {
        return inv.currentItem;
    }

    public static int getCurrentSlot(@Nonnull EntityPlayer ent) {
        return getCurrentSlot(getInventory(ent));
    }

    public static void setCurrentSlot(@Nonnull InventoryPlayer inv, int cur) {
        inv.currentItem = cur;
    }

    public static void setCurrentSlot(@Nonnull EntityPlayer ent, int cur) {
        setCurrentSlot(getInventory(ent), cur);
    }

    //-Item-------------------------------------------------------------------
    @Nonnull
    public static Item getItem(@Nonnull Block block) {
        return Item.getItemFromBlock(block);
    }

    @Nullable
    public static Item getItem(@Nullable ItemStack stack) {
        return stack == null ? null : stack.getItem();
    }

    @Nullable
    public static Item getItem(@Nonnull String name) {
        return Item.getByNameOrId(name);
    }

    @Nonnull
    public static Item getItem(int id) {
        //return Item.itemsList[id]; // < 1.8
        return Item.getItemById(id);
    }

    @Nonnull
    public static String getName(@Nonnull Item item) {
        return item.getUnlocalizedName();
    }

    @Deprecated
    public static int getId(@Nonnull Item item) {
        return Item.getIdFromItem(item);
    }

    public static int getItemIdMeta(int base, int meta) {
        return getIdMeta(base, meta);
    }

    @Deprecated
    public static int getItemId(int idmeta) {
        return getBase(idmeta);
    }

    public static int getItemMeta(int idmeta) {
        return getMeta(idmeta);
    }

    public static int getItemMax(@Nullable Item item) {
        return item == null ? 0 : item.getItemStackLimit();
    }

    public static void setItemMax(@Nullable Item item, int val) {
        if (item != null) {
            item.setMaxStackSize(val);
        }
    }

    public static int getItemDmgCap(@Nonnull Item item) {
        return item.getMaxDamage();
    }

    public static void setItemDmgCap(Item item, int val) {
        //item.setMaxDamage(val);
    }

    public static boolean hasSubTypes(@Nullable Item item) {
        return item != null && item.getHasSubtypes();
    }

    @Deprecated
    public static boolean getItemHasSubTypes(Item item) {
        return hasSubTypes(item);
    }

    //-ItemStack--------------------------------------------------------------
    // private static Icon getItemsIcon(ItemStack items) { return items.getIconIndex(); }
    @Nullable
    public static ItemStack getStack(@Nullable EntityItem item) {
        return item == null ? null : item.getEntityItem();
    }

    @Nullable
    public static ItemStack getStack(@Nullable Item item, int count, int meta) {
        //return new ItemStack(id, count, meta); // < 1.8
        return item == null ? null : new ItemStack(item, count, meta);
    }

    @Nullable
    @Deprecated
    public static ItemStack getStack(int id, int count, int meta) {
        //return new ItemStack(id, count, meta); // < 1.8
        return getStack(getItem(id), count, meta);
    }

    @Nullable
    public static ItemStack getStack(Item item, int count) {
        return getStack(item, count, 0);
    }

    @Nullable
    @Deprecated
    public static ItemStack getStack(int idmeta, int count) {
        return getStack(getStackId(idmeta), count, getStackMeta(idmeta));
    }

    @Deprecated
    public static int getId(ItemStack stack) {
        //return items.itemID; // < 1.8
        return getId(getItem(stack));
    }

    public static int getMeta(@Nullable ItemStack stack) {
        //return items.getItemDamage(); // alt
        return stack == null ? 0 : stack.getMetadata();
    }

    public static int getIdMeta(ItemStack stack) {
        return getStackIdMeta(getId(stack), getMeta(stack));
    }

    public static void setMeta(@Nonnull ItemStack stack, int meta) {
        stack.setItemDamage(meta);
    }

    @Deprecated
    public static int getStackIdMeta(int base, int meta) {
        return getIdMeta(base, meta);
    }

    @Deprecated
    public static int getStackId(int idmeta) {
        return getBase(idmeta);
    }

    @Deprecated
    public static int getStackMeta(int idmeta) {
        return getMeta(idmeta);
    }

    public static int getStackSize(ItemStack stack) {
        try {
            //noinspection ConstantConditions
            return (Integer) getValue(fStackSize, stack);
        } catch (NullPointerException e) {
            e.printStackTrace();
            exit(0);
            return 0;
        }
    }

    public static void setStackSize(ItemStack stack, int size) {
        setValue(fStackSize, stack, size);
    }

    @Deprecated
    public static boolean isStackMatch(int stack, int match) {
        return getStackId(stack) == getStackId(match) && (getStackMeta(stack) == getStackMeta(match) || getStackMeta(match) == ID_ANY);
    }

    public static boolean isStackMatch(ItemStack stack, int match) {
        return isStackMatch(getIdMeta(stack), match);
    }

    public static boolean isStackMatch(ItemStack stack, ItemStack match) {
        return isStackMatch(getIdMeta(stack), getIdMeta(match));
    }

    @Nullable
    public static ItemStack getGridItem(int nr) {
        GuiContainer menu = (GuiContainer) getMenu();
        Container slots = menu.inventorySlots;

        if (menu instanceof GuiCrafting) {
            return ((ContainerWorkbench) slots).craftMatrix.getStackInSlot(nr);
        } else if (menu instanceof GuiInventory) {
            return ((ContainerPlayer) slots).craftMatrix.getStackInSlot(nr);
        }

        return null;
    }

    //-Material---------------------------------------------------------------
    public static boolean getIsLiquid(@Nonnull Material mat) {
        return mat.isLiquid();
    }

    public static boolean getIsSolid(@Nonnull Material mat) {
        return mat.isSolid();
    }

    public static boolean getIsCover(@Nonnull Material mat) {
        //return mat.getCanBlockGrass(); < MCP 9.10
        return mat.blocksLight();
    }

    public static boolean getIsBurnable(@Nonnull Material mat) {
        return mat.getCanBurn();
    }

    public static boolean getIsReplaceable(@Nonnull Material mat) {
        return mat.isReplaceable();
    }

    //-Minecraft--------------------------------------------------------------
    @Nonnull
    public static Minecraft getMinecraft() {
        return Minecraft.getMinecraft();
    }

    public static File getDataDir() {
        return getMinecraft().mcDataDir;
    }

    @Nonnull
    public static String getLaunchedVersion() {
        return getMinecraft().getVersion();
    }

    // note: used to be getMap()
    public static WorldClient getWorld() {
        return getMinecraft().world;
    }

    public static EntityPlayerSP getPlayer() {
        return getMinecraft().player;
    }

    public static GameSettings getGameSettings() {
        return getMinecraft().gameSettings;
    }

    @Nullable
    public static GuiScreen getMenu() {
        return getMinecraft().currentScreen;
    }

    public static void setMenu(GuiScreen menu) {
        getMinecraft().displayGuiScreen(menu);
    }

    // note: used to be getIsMenu()
    public static boolean isInMenu() {
        return getMenu() != null;
    }

    // note: used to be getIsOptions()
    public static boolean isInOptions() {
        return getMenu() instanceof ConfigurationScreen;
    }

    // note: used to be getIsMultiplayer()
    public static boolean isMultiplayer() {
        return !getMinecraft().isSingleplayer();
    }

    public static int getScreenWidth() {
        return getMinecraft().displayWidth;
    }

    public static int getScreenHeight() {
        return getMinecraft().displayHeight;
    }

    @Nonnull
    public static ScaledResolution getScaledResolution() {
        return new ScaledResolution(getMinecraft());
    }

    //note: used to be getScrWidthS()
    public static int getScaledWidth() {
        return getScaledResolution().getScaledWidth();
    }

    //note: used to be getScrHeightS()
    public static int getScaledHeight() {
        return getScaledResolution().getScaledHeight();
    }

    public static double getScaledWidthD() {
        return getScaledResolution().getScaledWidth_double();
    }

    public static double getScaledHeightD() {
        return getScaledResolution().getScaledHeight_double();
    }

    //private static int getTexture(String name) { return getMinecraft().renderEngine.getTexture(name); }
    /*public static void bindTexture(String name) {
        getMinecraft().func_110434_K().func_110577_a(new ResourceLocation("textures"+name));
    }*/
    public static PlayerControllerMP getPlayerController() {
        return getMinecraft().playerController;
    }

    public static void refreshTextures() {
        //getMinecraft().func_110436_a(); // 1.6.2 MCP 8.04
        getMinecraft().refreshResources();
    }

    //public static Field fAchivement = getField(GuiAchievement.class, "ga_theAchievement");
    public static EntityRenderer getRenderer() {
        return getMinecraft().entityRenderer;
    }

    @Nonnull
    public static ItemRenderer getItemRenderer() {
        return getMinecraft().getItemRenderer();
    }

    @Nullable
    public static Entity getView() {
        //return getMinecraft().renderViewEntity; // < 1.8
        return getMinecraft().getRenderViewEntity(); // 1.8 MCP 9.10
    }

    public static void setView(@Nonnull Entity ent) {
        //minecraft.renderViewEntity = ent; // < 1.8
        //getMinecraft().func_175607_a(ent); // 1.8 MCP 9.10
        getMinecraft().setRenderViewEntity(ent);
    }

    @Nonnull
    public static String getPath() {
        try {
            return getDataDir().getCanonicalPath();
        } catch (Exception e) {
            return "";
        }
    }

    //-RayTraceResult---------------------------------------------------------
    @Nullable
    public static RayTraceResult getMOP() {
        return new RayTraceResult(RayTraceResult.Type.MISS, new Vec3d(0, 0, 0), null, null);
    }

    @Nullable
    public static Entity getEntity(@Nullable RayTraceResult mop) {
        return mop == null ? null : mop.entityHit;
    }

    @Nullable
    public static BlockFace getBlockFace(@Nullable RayTraceResult mop) {
        if (mop == null) {
            return null;
        }
        return getSide(mop) == -1 ? null : new BlockFace(getPos(mop), getFacing(mop));
    }

    @Nonnull
    public static BlockPos getPos(@Nonnull RayTraceResult mop) {
        return mop.getBlockPos();
    }

    public static int getX(@Nonnull RayTraceResult mop) {
        //return mop.blockX; // < 1.8
        return getX(getPos(mop));
    }

    public static int getY(@Nonnull RayTraceResult mop) {
        return getY(getPos(mop));
    }

    public static int getZ(@Nonnull RayTraceResult mop) {
        return getZ(getPos(mop));
    }

    public static EnumFacing getFacing(@Nonnull RayTraceResult mop) {
        return mop.sideHit;
    }

    public static int getSide(@Nonnull RayTraceResult mop) {
        //return mop.sideHit; // < 1.8
        return getFacing(getFacing(mop));
    }

    //-NetHandlerPlayCLient---------------------------------------------------
    //-NetClientHandler-------------------------------------------------------
    @Nullable
    public static NetHandlerPlayClient getSendQueue() {
        return getMinecraft().getConnection();
    }

    public static void queuePacket(@Nonnull NetHandlerPlayClient queue, @Nonnull Packet<?> packet) {
        queue.sendPacket(packet);
    }

    public static void queuePacket(@Nonnull Packet<?> packet) {
        queuePacket(getSendQueue(), packet);
    }

    //-PlayerControllerMP-----------------------------------------------------
    public static void syncCurrentItem(@Nonnull PlayerControllerMP controller) {
        controller.syncCurrentItem();
    }

    public static void syncCurrentItem() {
        syncCurrentItem(getPlayerController());
    }

    //-RenderItem-------------------------------------------------------------
    @Nonnull
    public static RenderItem getRenderItem() {
        return getMinecraft().getRenderItem();
    }

    public static void renderItemGUI(int x, int y, @Nonnull ItemStack stack) {
        //getRenderItem().renderItemIntoGUI(getFontRenderer(), getTextureManager(), stack, x, y); < 1.8
        //getRenderItem().func_175030_a(getFontRenderer(), stack, x, y); //? MCP 9.10
        getRenderItem().renderItemIntoGUI(stack, x, y);
    }

    //-TextureManager---------------------------------------------------------
    @Nonnull
    public static TextureManager getTextureManager() {
        //return getMinecraft().renderEngine; // < 1.6
        //return getMinecraft().func_110434_K(); // 1.6.2 MCP 8.04
        return getMinecraft().getTextureManager();
    }

    //-TileEntity-------------------------------------------------------------
    public static void setChanged(@Nonnull TileEntity tent) {
        //tent.onInventoryChanged(); // < 1.8 MCP 9.10
        tent.markDirty();
    }

    @Nullable
    public static NBTTagCompound getTileEntityCopy(@Nullable TileEntity ent) {
        if (ent == null) {
            return null;
        }
        NBTTagCompound nbt = new NBTTagCompound();
        ent.writeToNBT(nbt);
        return nbt;
    }

    @Nonnull
    public static TileEntity setPos(@Nonnull TileEntity tent, int x, int y, int z) {
        return setPos(tent, new BlockPos(x, y, z));
    }

    @Nonnull
    public static TileEntity setPos(@Nonnull TileEntity tent, @Nonnull BlockPos pos) {
        tent.setPos(pos);
        return tent;
    }

    @Nonnull
    public static NBTTagCompound setPos(@Nonnull NBTTagCompound nbt, int x, int y, int z) {
        nbt.setInteger("x", x);
        nbt.setInteger("y", y);
        nbt.setInteger("z", z);
        return nbt;
    }
    /*
    private static void loadTileEntityFromNBT(ZP250 obj) {
        try {
        NBTTagCompound data = obj.nbtData;
        NBTTagCompound.writeNamedTag(data, new DataOutputStream(new ByteArrayOutputStream())); // NBT stuff probably never changes
        TileEntity ent = TileEntity.createAndLoadEntity(data);
        if (map != getWorld()) return; // just in case
        map.setBlockTileEntity(ent.xCoord, ent.yCoord, ent.zCoord, ent);
        } catch(Exception fuckoffyoufuckingpiceofshitofajavaretardationidonotcare) {}
    }
    */

    @Nullable
    public static TileEntity getTileEntityFromCopy(@Nullable NBTTagCompound nbt) {
        if (nbt == null) {
            return null;
        }
        //return TileEntity.createAndLoadEntity(nbt);
        return TileEntity.create(getWorld(), nbt);
    }

    public static void setTileEntityFromCopy(@Nonnull TileEntity ent, @Nonnull NBTTagCompound nbt) {
        ent.readFromNBT(nbt);
    }

    public static void setTileEntityFromCopy(@Nonnull World world, int x, int y, int z, @Nonnull NBTTagCompound nbt) {
        setPos(nbt, x, y, z);
        TileEntity tent = getTileEntityAt(world, x, y, z);
        if (tent == null) {
            setTileEntityAt(world, getTileEntityFromCopy(nbt), x, y, z);
        } else {
            setTileEntityFromCopy(tent, nbt);
        }
        //setPos(tent, x,y,z);
    }

    @Nullable
    public static ItemStack[] getChestItems(Object tent) {
        return (ItemStack[]) getValue(fChestItems, tent);
    }

    @Nonnull
    public static ItemStack[] getDispItems(TileEntityDispenser tent) {
        NonNullList<ItemStack> items = (NonNullList<ItemStack>) getValue(fDispItems, tent);
        assert items != null;
        return toISArray(items);
    }

    @Nullable
    public static ItemStack[] getFurnaceItems(Object tent) {
        return (ItemStack[]) getValue(fFurnaceItems, tent);
    }

    //-TileEntitySign---------------------------------------------------------
    @Nonnull
    public static String[] getSignText(@Nonnull TileEntitySign tent) {
        //return tent.signText; // < 1.8
        ITextComponent[] components = tent.signText;
        String[] text = new String[components.length];
        for (int i = 0; i < text.length; ++i) {
            text[i] = components[i].getUnformattedText();
        }
        return text;
    }

    @Nullable
    public static String[] getSignText(int x, int y, int z) {
        TileEntity tent = getTileEntityAt(getWorld(), x, y, z);
        if (tent instanceof TileEntitySign) {
            return getSignText((TileEntitySign) tent);
        }
        return null;
    }

    //-Vec3-------------------------------------------------------------------
    public static double getX(@Nonnull Vec3d pos) {
        return pos.xCoord;
    }

    public static double getY(@Nonnull Vec3d pos) {
        return pos.yCoord;
    }

    public static double getZ(@Nonnull Vec3d pos) {
        return pos.zCoord;
    }

    //-Vec3i/BlockPos---------------------------------------------------------
    public static int getX(@Nonnull Vec3i pos) {
        return pos.getX();
    }

    public static int getY(@Nonnull Vec3i pos) {
        return pos.getY();
    }

    public static int getZ(@Nonnull Vec3i pos) {
        return pos.getZ();
    }

    //-World-- ---------------------------------------------------------------
    @Nonnull
    public static List<Entity> getEntities() {
        //return getWorld().loadedEntityList.clone();
        return new ArrayList<Entity>(getWorld().getLoadedEntityList());
    }

    @Nonnull
    public static Chunk getChunkFromBlockCoords(@Nonnull World world, @Nonnull BlockPos pos) {
        return world.getChunkFromBlockCoords(pos);
    }

    @Nonnull
    public static Chunk getChunkFromBlockCoords(@Nonnull World world, int x, int z) {
        return world.getChunkFromChunkCoords(x >> 4, z >> 4);
    }

    @Nonnull
    public static Chunk getChunkFromBlockCoords(@Nonnull BlockPos pos) {
        return getChunkFromBlockCoords(getWorld(), pos);
    }

    @Nonnull
    public static Chunk getChunkFromBlockCoords(int x, int z) {
        return getChunkFromBlockCoords(getWorld(), x, z);
    }

    @Nonnull
    public static IBlockState getStateAt(@Nonnull World world, int x, int y, int z) {
        return getStateAt(world, new BlockPos(x, y, z));
    }

    @Nonnull
    public static IBlockState getStateAt(@Nonnull World world, @Nonnull BlockPos pos) {
        return world.getBlockState(pos);
    }

    public static void setStateAt(@Nonnull World world, @Nonnull IBlockState state, int flags, int x, int y, int z) {
        setStateAt(world, state, flags, new BlockPos(x, y, z));
    }

    public static void setStateAt(@Nonnull World world, @Nonnull IBlockState state, int flags, @Nonnull BlockPos pos) {
        world.setBlockState(pos, state, flags);
    }

    // note: used to be getWorldBlock()
    @Nullable
    public static Block getBlockAt(@Nonnull World world, int x, int y, int z) {
        //return getBlock(world.getBlockId(x,y,z)); // < 1.8
        return getBlock(getStateAt(world, x, y, z));
    }

    @Nullable
    public static Block getBlockAt(@Nonnull World world, @Nonnull BlockPos pos) {
        //return getBlock(world.getBlockId(x,y,z)); // < 1.8
        //return getChunkFromBlockCoords(world, pos).getBlock(pos); // alt
        return getBlock(getStateAt(world, pos));
    }

    // note: used to be getWorldId()
    @Deprecated
    public static int getIdAt(@Nonnull World world, int x, int y, int z) {
        //return world.getBlockId(x,y,z); // < 1.8
        return getId(getStateAt(world, x, y, z));
    }

    @Deprecated
    public static int getIdAt(@Nonnull World world, @Nonnull BlockPos pos) {
        return getId(getStateAt(world, pos));
    }

    // note: used to be getWorldMeta()
    @Deprecated
    public static int getMetaAt(@Nonnull World world, int x, int y, int z) {
        //return world.getBlockMetadata(x,y,z); // < 1.8
        return getMeta(getStateAt(world, x, y, z));
    }

    @Deprecated
    public static int getMetaAt(@Nonnull World world, @Nonnull BlockPos pos) {
        //return getChunkFromBlockCoords(world, pos).getBlockMetaData(pos); // alt
        return getMeta(getStateAt(world, pos));
    }

    @Deprecated
    public static int getIdMetaAt(@Nonnull World world, int x, int y, int z) {
        return getBlockIdMeta(getStateAt(world, x, y, z));
    }

    @Deprecated
    public static int getIdMetaAt(@Nonnull World world, @Nonnull BlockPos pos) {
        return getBlockIdMeta(getStateAt(world, pos));
    }

    // note: used to be getWorldTileEntity()
    @Nullable
    public static TileEntity getTileEntityAt(@Nonnull World world, int x, int y, int z) {
        //return world.getBlockTileEntity(x,y,z); // < 1.8
        return getTileEntityAt(world, new BlockPos(x, y, z));
    }

    @Nullable
    public static TileEntity getTileEntityAt(@Nonnull World world, @Nonnull BlockPos pos) {
        return world.getTileEntity(pos);
    }

    public static void setBlockAt(@Nonnull World world, Block block, int flags, int x, int y, int z) {
        //world.setBlock(x,y,z, id); // < 1.8
        setStateAt(world, getDefaultState(block), flags, x, y, z);
    }

    public static void setBlockAt(@Nonnull World world, Block block, int flags, @Nonnull BlockPos pos) {
        //world.setBlock(x,y,z, id); // < 1.8
        setStateAt(world, getDefaultState(block), flags, pos);
    }

    @Deprecated
    public static void setIdAt(@Nonnull World world, int id, int flags, int x, int y, int z) {
        //world.setBlock(x,y,z, id, meta, 1); // < 1.8
        setStateAt(world, getDefaultState(id), flags, x, y, z);
    }

    @Deprecated
    public static void setIdAt(@Nonnull World world, int id, int flags, @Nonnull BlockPos pos) {
        //world.setBlock(x,y,z, id, meta, 0); // < 1.8
        setStateAt(world, getDefaultState(id), flags, pos);
    }

    @Deprecated
    public static void setIdMetaAt(@Nonnull World world, int id, int meta, int flags, int x, int y, int z) {
        //world.setBlock(x,y,z, id, meta, 1); // < 1.8
        setStateAt(world, getState(id, meta), flags, x, y, z);
    }

    @Deprecated
    public static void setIdMetaAt(@Nonnull World world, int id, int meta, int flags, @Nonnull BlockPos pos) {
        //world.setBlock(x,y,z, id, meta, 0); // < 1.8
        setStateAt(world, getState(id, meta), flags, pos);
    }

    @Deprecated
    public static void setIdMetaAt(@Nonnull World world, int idmeta, int flags, int x, int y, int z) {
        //world.setBlock(x,y,z, id, meta, 1); // < 1.8
        setStateAt(world, getState(idmeta), flags, x, y, z);
    }

    @Deprecated
    public static void setIdMetaAt(@Nonnull World world, int idmeta, int flags, @Nonnull BlockPos pos) {
        //world.setBlock(x,y,z, id, meta, 0); // < 1.8
        setStateAt(world, getState(idmeta), flags, pos);
    }

    public static void setTileEntityAt(@Nonnull World world, TileEntity tent, int x, int y, int z) {
        setTileEntityAt(world, tent, new BlockPos(x, y, z));
    }

    public static void setTileEntityAt(@Nonnull World world, TileEntity tent, @Nonnull BlockPos pos) {
        world.setTileEntity(pos, tent);
    }

    public static void notifyBlock(@Nonnull World world, @Nonnull BlockPos pos) {
        //        world.notifyBlockOfStateChange(pos, getBlockAt(world, pos));
        world.func_190522_c(pos, getBlockAt(world, pos)); // ??? TODO
    }

    public static void notifyBlock(@Nonnull World world, int x, int y, int z) {
        //world.notifyBlockOfNeighborChange(x,y,z, getIdAt(world,x,y,z)); // < 1.8
        notifyBlock(world, new BlockPos(x, y, z));
    }

    public static void notifyNeighbors(@Nonnull World world, @Nonnull BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, getBlockAt(world, pos), false);
    }

    public static void notifyNeighbors(@Nonnull World world, int x, int y, int z) {
        //world.notifyBlocksOfNeighborChange(x,y,z, getIdAt(world,x,y,z)); // < 1.8
        notifyNeighbors(world, new BlockPos(x, y, z));
    }

    public static void markForUpdate(@Nonnull World world, @Nonnull BlockPos pos) {
        //world.markBlockForUpdate(pos);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public static void markForUpdate(@Nonnull World world, int x, int y, int z) {
        //world.markBlockForUpdate(x,y,z); // < 1.8
        markForUpdate(world, new BlockPos(x, y, z));
    }

    public static void markForUpdate(@Nonnull World world, @Nonnull BlockPos start, @Nonnull BlockPos end) {
        world.markBlockRangeForRenderUpdate(start, end);
    }

    public static void markForUpdate(@Nonnull World world, int sx, int sy, int sz, int ex, int ey, int ez) {
        world.markBlockRangeForRenderUpdate(sx, sy, sz, ex, ey, ez);
    }

    public static void notifyAndMark(@Nonnull World world, @Nonnull BlockPos pos) {
        notifyNeighbors(world, pos);
        markForUpdate(world, pos);
    }

    public static void notifyAndMark(@Nonnull World world, int x, int y, int z) {
        notifyAndMark(world, new BlockPos(x, y, z));
    }

    public static void notifyAndMark(@Nonnull World world, int sx, int sy, int sz, int ex, int ey, int ez) {
        //markForUpdate(world, sx,sy,sz, ex,ey,ez);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                for (int z = sz; z <= ez; ++z) {
                    //notifyNeighbors(world, x,y,z);
                    //markForUpdate(world, x,y,z);
                    notifyAndMark(world, new BlockPos(x, y, z));
                }
            }
        }
    }

    public static void spawnLightning(int x, int y, int z) {
        getWorld().spawnEntityInWorld(new EntityLightningBolt(getWorld(), x, y, z, false));
    }

    // light functions taken from the F3 debug screen (GuiOverlayDebug)
    public static int getBlockLightLevel(@Nonnull BlockPos pos) {
        return getChunkFromBlockCoords(pos).getLightFor(EnumSkyBlock.BLOCK, pos);
    }

    public static int getBlockLightLevel(int x, int y, int z) {
        //return getChunkFromBlockCoords(x,z).getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15); // < 1.8
        return getBlockLightLevel(new BlockPos(x, y, z));
    }

    public static int getSkyLightLevel(@Nonnull BlockPos pos) {
        return getChunkFromBlockCoords(pos).getLightFor(EnumSkyBlock.SKY, pos);
    }

    public static int getSkyLightLevel(int x, int y, int z) {
        //return getChunkFromBlockCoords(x,z).getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15); // < 1.8
        return getSkyLightLevel(new BlockPos(x, y, z));
    }

    public static int getRealLightLevel(@Nonnull BlockPos pos) {
        //return getChunkFromBlockCoords(pos).setLight(pos,0); // MCP 9.10
        getChunkFromBlockCoords(pos).getLightFor(EnumSkyBlock.BLOCK, pos);
        return getChunkFromBlockCoords(pos).getLightFor(EnumSkyBlock.BLOCK, pos);
    }

    public static int getRealLightLevel(int x, int y, int z) {
        //return getChunkFromBlockCoords(x,z).getBlockLightValue(x & 15, y, z & 15, 0); // < 1.8
        return getRealLightLevel(new BlockPos(x, y, z));
    }

    @Nonnull
    public static List<AxisAlignedBB> getCollidingBlockAABBs(@Nonnull World world, @Nonnull AxisAlignedBB aabb) {
        //return world.getCollidingBlockBounds(aabb); // < 1.8
        //return world.func_147461_a(aabb); // MCP 9.10
        return world.getCollisionBoxes(null, aabb);
    }

    public static boolean canMonsterSpawnAt(int x, int y, int z) {
        //return SpawnerAnimals.canCreatureTypeSpawnAtLocation(EnumCreatureType.MONSTER, getWorld(), x,y,z); // < 1.8
        //return SpawnerAnimals.func_180267_a(EntityLiving.SpawnPlacementType.ON_GROUND, getWorld(), new BlockPos(x,y,z)); // 1.8
        EntityLiving.SpawnPlacementType type = EntityLiving.SpawnPlacementType.ON_GROUND;
        return WorldEntitySpawner.canCreatureTypeSpawnAtLocation(type, getWorld(), new BlockPos(x, y, z));
    }

    public static long getTime(@Nonnull World world) {
        return world.getWorldTime();
    }

    public static long getTime() {
        return getTime(getWorld());
    }

    public static void setTime(long val) {
        setTime(getWorld(), val);
    }

    public static void setTime(@Nonnull World world, long val) {
        world.setWorldTime(val);
    }

    //-WorldInfo--------------------------------------------------------------
    @Nonnull
    public static WorldInfo getWorldInfo(@Nonnull World world) {
        return world.getWorldInfo();
    }

    @Nonnull
    public static WorldInfo getWorldInfo() {
        return getWorldInfo(getWorld());
    }

    public static boolean getRaining() {
        return getWorldInfo().isRaining();
    }

    public static void setRaining(boolean val) {
        getWorldInfo().setRaining(val);
    }

    public static boolean getThunder() {
        return getWorldInfo().isThundering();
    }

    public static void setThunder(boolean val) {
        getWorldInfo().setThundering(val);
    }

    public static int getRainingTime() {
        return getWorldInfo().getRainTime();
    }

    public static void setRainingTime(int val) {
        getWorldInfo().setRainTime(val);
    }

    public static int getThunderTime() {
        return getWorldInfo().getThunderTime();
    }

    public static void setThunderTime(int val) {
        getWorldInfo().setThunderTime(val);
    }

    public static long getSeed() {
        return getWorldInfo().getSeed();
    }

    public static int getSpawnX() {
        return getWorldInfo().getSpawnX();
    }

    public static int getSpawnY() {
        return getWorldInfo().getSpawnY();
    }

    public static int getSpawnZ() {
        return getWorldInfo().getSpawnZ();
    }

    @Nonnull
    public static String getWorldName() {
        return getWorldInfo().getWorldName();
    }

    //------------------------------------------------------------------------
    private static void setXItemLighting() {
        RenderHelper.enableStandardItemLighting();
    }

    public static boolean getIsHell() {
        return getIdAt(getWorld(), fix(getPlayer().posX), 127, fix(getPlayer().posZ)) == 7;
    } // hackish, there is certainly a better way - but the hell with it.

    /**
     * Returns correct integer coordinate
     */
    public static int fix(double d) {
        return (int) Math.floor(d);
    }


    /* HELPERS */

    public static int getIdMeta(int base, int meta) {
        return (base & ID_BASE) | (((meta < 0 || meta == ID_ANY) ? ID_ANY : meta & ID_ONE) << 16);
    }

    public static int getBase(int idmeta) {
        return idmeta & ID_BASE;
    }

    public static int getMeta(int idmeta) {
        return (idmeta >> 16) & ID_META;
    }

    /**
     * Returns true if the key is down
     */
    public static boolean isKeyDownThisTick(int key) {
        return Keys.isKeyDownThisTick(key);
    }

    /**
     * Returns true if the key is down
     */
    public static boolean isKeyDownThisFrame(int key) {
        return Keys.isKeyDownThisFrame(key);
    }

    /**
     * Returns true if the key is pressed
     */
    public static boolean wasKeyPressedThisTick(int key) {
        return Keys.wasKeyPressedThisTick(key);
    }

    /**
     * Returns true if the key is pressed
     */
    public static boolean wasKeyPressedThisFrame(int key) {
        return Keys.wasKeyPressedThisFrame(key);
    }

    public float getEntityMoveSpeed(@Nonnull EntityLivingBase ent) {
        return ent.getAIMoveSpeed();
    }

    public void setEntityMoveSpeed(@Nonnull EntityLivingBase ent, float val) {
        ent.setAIMoveSpeed(val);
    }

}

