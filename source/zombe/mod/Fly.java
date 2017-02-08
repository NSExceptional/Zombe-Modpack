package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import zombe.core.*;
import zombe.core.content.DummyPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Fly extends ZMod {

    private static final double TO_RADIANS = Math.PI / 180f;

    private static boolean modFlyAllowed = true, modNoclipAllowed = true;
    private static String tagFly, tagNoclip;
    private static int keyOn, keyOff, keyToggle, keyNoclip, keySpeed, keyRun, keyUp, keyDown, keyFreeFly, keyForward, keyBackward;
    private static float optSpeedVertical, optSpeedForward, optSpeedMulNormal, optSpeedMulModifier, optRunSpeedMul, optRunSpeedVMul, optJump, optJumpHigh;
    private static boolean optNoclip, optFreeFly, optAirJump,
        optVanillaFly, optVanillaSprint, optNoInertia,
        optSpeedIsToggle, optRunSpeedIsToggle,
        optFixMovedWrongly, optFixMovedTooQuickly;
    private static boolean playerFly, playerNoclip, playerSpeed, playerRun;
    private static boolean dummyFly, dummyNoclip, dummySpeed, dummyRun;
    private static boolean flying, noclip, flyRun, flySpeed, flyUp, flyDown, flyForward, flyFree;
    private static boolean playerClassActive = false, flew = false, playerOnGround, dummyOnGround, controllingPlayer;
    @Nullable private static Entity controlledEntity;
    @Nullable private static EntityPlayerSP flyPlayer;
    @Nullable private static DummyPlayer flyDummy;
    private static float flySteps;
    private static double motionX, motionY, motionZ;

    public Fly() {
        super("fly", "1.8", "9.0.2");
        this.registerHandler("isFlying");
        this.registerHandler("isNoclip");
        this.registerHandler("isPlayerOnGround");
        this.registerHandler("allowVanillaFly");
        this.registerHandler("allowVanillaSprint");
        this.registerListener("onPlayerJump");
        this.registerListener("onViewUpdate");
        this.registerListener("onClientUpdate");
        this.registerListener("onServerUpdate");
        this.registerHandler("beforePlayerMove");
        this.registerHandler("afterPlayerMove");
        this.registerHandler("beforeViewMove");
        this.registerHandler("afterViewMove");
        this.registerHandler("ignorePlayerInsideOpaqueBlock");

        this.addOption("fly config");
        this.addOption("keyFlyOn", "Turns fly mode on", Keyboard.KEY_NONE);
        this.addOption("keyFlyOff", "Turns fly mode off", Keyboard.KEY_NONE);
        this.addOption("keyFlyToggle", "Toggles fly mode", Keyboard.KEY_F);
        this.addOption("tagFly", "Tag shown when fly activated", "flying");
        this.addOption("keyFlyNoClip", "Toggles noclip mode", Keyboard.KEY_F6);
        this.addOption("optFlyNoClip", "Enable noclip mode by default", false);
        this.addOption("tagFlyNoClip", "Tag shown when noclip activated", "noclip");
        this.addOption("optFlyVanillaFly", "Allow vanilla MC fly toggle", true);
        this.addOption("optFlyAirJump", "Allow air jumps", true);
        this.addOption("optFlyNoInertia", "(broken) Disable inertia when flying", false);

        this.addOption("fly controls");
        this.addOption("keyFlyUp", "Fly up", Keyboard.KEY_E);
        this.addOption("keyFlyDown", "Fly down", Keyboard.KEY_Q);
        this.addOption("optFlySpeedVertical", "Vertical flying speed", 0.2f, 0.1f, 1f, true);
        this.addOption("optFlySpeedMulNormal", "Flying speed multiplier", 1f, 0.1f, 10f, true);
        this.addOption("optFlySpeedMulModifier", "Flying speed multiplier with modifier", 4f, 0.1f, 10f, true);
        this.addOption("keyFlySpeed", "Flying speed modifier key", Keyboard.KEY_LSHIFT);
        this.addOption("optFlySpeedIsToggle", "Flying speed modifier is a toggle", false);
        this.addOption("keyFlyFreeFly", "Toggles Free Fly (non-horizontal fly)", Keyboard.KEY_NONE);
        this.addOption("optFlyFreeFly", "Enable Free Fly by default", false);

        this.addOption("sprint controls");
        this.addOption("optFlyJump", "Jump speed multiplier", 1f, 1f, 10f, true);
        this.addOption("optFlyJumpHigh", "Jump speed multiplier with modifier", 1.25f, 1f, 100f, true);
        this.addOption("optFlyRunSpeedMul", "Run speed multiplier with modifier", 1.5f, 0.1f, 10f, true);
        this.addOption("optFlyRunSpeedVMul", "Vertical speed gain with modifier, in ladder or fluid", 1.5f, 0.1f, 10f, true);
        this.addOption("keyFlyRun", "Running speed modifier key", Keyboard.KEY_LSHIFT);
        this.addOption("optFlyRunSpeedIsToggle", "Run speed modifier is a toggle", false);
        this.addOption("optFlyVanillaSprint", "Allow vanilla MC sprint toggle", true);
    }

    private static boolean isFlying(Object arg) {
        return arg == flyPlayer && playerFly || arg == flyDummy && dummyFly || arg instanceof Boolean && flying;
    }

    private static boolean isNoclip(Object arg) {
        if (arg == flyPlayer && playerFly && playerNoclip && !isMultiplayer()) {
            return true;
        }

        if (arg == flyDummy && dummyFly && dummyNoclip) {
            return true;
        }

        if (arg instanceof EntityPlayerMP) {
            if (isServerPlayer((EntityPlayerMP) arg) && playerFly && playerNoclip) {
                return true;
            }
        }
        if (arg instanceof Boolean) {
            if (flying && noclip && (!isMultiplayer() || controlledEntity != flyPlayer)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowVanillaFly() {
        return !modFlyAllowed || optVanillaFly;
    }

    private static boolean allowVanillaSprint() {
        return !modFlyAllowed || optVanillaSprint;
    }

    private static boolean isPlayerOnGround() {
        if (playerClassActive && flyPlayer != null) {
            return playerOnGround;
        } else {
            return flyPlayer == null || getOnGround(flyPlayer);
        }
    }

    @Nonnull
    private static Vec3d getServerMotion() {
        if (playerClassActive && flyPlayer != null) {
            return new Vec3d(motionX, motionY, motionZ);
        } else {
            return (flyPlayer != null) ? getMotion(flyPlayer) : new Vec3d(0, 0, 0);
        }
    }

    private static void onClientUpdate(EntityPlayerSP ent) {
        if (flyPlayer != ent) {
            return;
        }
        if (modFlyAllowed && optAirJump) {
            setOnGround(flyPlayer, true);
        } else if (playerClassActive && (!playerFly || !modFlyAllowed)) {
            setOnGround(flyPlayer, playerOnGround); // avoids air-jumping
        }
    }

    private static void onViewUpdate(EntityPlayer ent) {
        if (flyDummy != ent) {
            return;
        }
        if (optAirJump) {
            setOnGround(flyDummy, true);
        } else if (!dummyFly) {
            setOnGround(flyDummy, dummyOnGround); // avoids air-jumping
        }
    }

    private static void onServerUpdate(@Nonnull EntityPlayerMP ent) {
        if (!modFlyAllowed || flyPlayer == null) {
            return;
        }
        setNoclip(ent, getNoclip(flyPlayer)); // necessary for noclip

        if (!flyPlayer.capabilities.allowFlying) {
            setFlying(ent, getFlying(flyPlayer)); // necessary for damage-over-lava bug
        }
    }

    private static void onPlayerJump(@Nonnull EntityPlayer ent) {
        if (ent == flyPlayer && !modFlyAllowed) {
            return;
        }
        ent.motionY *= getJumpMultiplier();
    }

    private static float getJumpMultiplier() {
        return flyRun ? optJumpHigh : optJump;
    }

    // note: used to be flyHandle()
    @Nonnull
    private static Vec3d beforeMove(@Nonnull EntityPlayer ent, @Nonnull Vec3d move) {
        if (isSleeping(ent) || ent.isRiding()) {
            return move;
        }
        double mx = motionX = getX(move),
                my = motionY = getY(move),
                mz = motionZ = getZ(move);
        flySteps = 0;
        if (modFlyAllowed && ent == flyPlayer || ent == flyDummy) {
            flySteps = getSteps(ent);
            if (ent == flyPlayer && playerFly || ent == flyDummy && dummyFly) {
                if (ent == flyPlayer) {
                    flyPlayer.movementInput.sneak = false;
                } else {
                    flyDummy.movementInput.sneak = false;
                }
                my = 0d;
                if (optNoInertia || ent != controlledEntity) {
                    mx = my = mz = 0;
                }
                if (ent == controlledEntity) {
                    if (flyUp)   my += optSpeedVertical;
                    if (flyDown) my -= optSpeedVertical;
                    if (flyFree) {
                        double siny = Math.sin(getYaw(ent) * TO_RADIANS);
                        double cosy = Math.cos(getYaw(ent) * TO_RADIANS);
                        double sinp = Math.sin(getPitch(ent) * TO_RADIANS);
                        double cosp = Math.cos(getPitch(ent) * TO_RADIANS);
                        double mf = -mx * siny + mz * cosy;
                        double mv = -mf * sinp;
                        mf *= 1 - cosp;
                        mx -= -mf * siny;
                        mz -= mf * cosy;
                        my += mv;
                    }
                    if (flyForward) {
                        double moves = optSpeedForward;
                        double movef = -moves * Math.cos(getPitch(ent) * TO_RADIANS);
                        double movev = -moves * Math.sin(getPitch(ent) * TO_RADIANS);
                        mx += movef * Math.sin(getYaw(ent) * TO_RADIANS);
                        mz += -movef * Math.cos(getYaw(ent) * TO_RADIANS);
                        my += movev;
                    }
                    double mul = flySpeed ? optSpeedMulModifier : optSpeedMulNormal;
                    if (optNoInertia) {
                        mul *= 2;
                    }
                    mx *= mul;
                    my *= mul;
                    mz *= mul;
                }
                setFall(ent, 0f);
                if (ent == flyPlayer) {
                    flew = true;
                }
            } else if (ent == controlledEntity && flyRun) {
                mx *= optRunSpeedMul;
                mz *= optRunSpeedMul;
                int id = getIdAt(getWorld(), fix(getX(ent)), fix(getY(ent)), fix(getZ(ent)));
                // if in ladders, water or lava
                if (id == 65 || id >= 8 && id <= 11) {
                    my *= optRunSpeedVMul;
                }
            }
        }
        setMotion(ent, mx, my, mz);
        return new Vec3d(mx, my, mz);
    }

    private static void afterMove(@Nonnull EntityPlayer ent, Vec3d move) {
        if (isSleeping(ent) || ent.isRiding()) {
            return;
        }
        if (ent == flyPlayer) {
            playerClassActive = true;
            playerOnGround = getOnGround(ent);

            if (modFlyAllowed) {
                if (getMotionX(ent) != 0) setMotionX(ent, motionX);
                if (getMotionY(ent) != 0) setMotionY(ent, motionY);
                if (getMotionZ(ent) != 0) setMotionZ(ent, motionZ);
                if (playerFly) {
                    flyPlayer.movementInput.sneak = false;
                    setFall(ent, 0f);
                    setOnGround(ent, true);
                    setSteps(ent, flySteps);
                    setFlying(ent, true);
                    if (ent.capabilities.allowFlying) {
                        ent.sendPlayerAbilities();
                    }
                } else if (flew && !getOnGround(ent)) {
                    setFall(ent, 0f);
                    setOnGround(ent, true);
                    if (ent.capabilities.allowFlying) {
                        ent.sendPlayerAbilities();
                    }
                } else {
                    flew = false;
                }
            }
        }
        if (ent == flyDummy) {
            dummyOnGround = getOnGround(ent);
            if (getMotionX(ent) != 0) setMotionX(ent, motionX);
            if (getMotionY(ent) != 0) setMotionY(ent, motionY);
            if (getMotionZ(ent) != 0) setMotionZ(ent, motionZ);
            if (dummyFly) {
                flyDummy.movementInput.sneak = false;
                setSteps(ent, flySteps);
                setFlying(ent, true);
            }
            setFall(ent, 0f);
            setOnGround(ent, true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("ignorePlayerInsideOpaqueBlock")) {
            return getNoclip();
        }
        if (name.equals("allowVanillaFly")) {
            return allowVanillaFly();
        }
        if (name.equals("allowVanillaSprint")) {
            return allowVanillaSprint();
        }
        if (name.equals("isFlying")) {
            return isFlying(arg);
        }
        if (name.equals("isNoclip")) {
            return isNoclip(arg);
        }
        if (name.equals("isPlayerOnGround")) {
            return isPlayerOnGround();
        }
        if (name.equals("beforePlayerMove")) {
            return beforeMove(getPlayer(), (Vec3d) arg);
        }
        if (name.equals("afterPlayerMove")) {
            afterMove(ZWrapper.getPlayer(), (Vec3d) arg);
        }
        if (name.equals("beforeViewMove")) {
            return beforeMove((EntityPlayer) getView(), (Vec3d) arg);
        }
        if (name.equals("afterViewMove")) {
            afterMove((EntityPlayer) getView(), (Vec3d) arg);
        }
        if (name.equals("onPlayerJump")) {
            onPlayerJump((EntityPlayer) arg);
        }
        if (name.equals("onClientUpdate")) {
            onClientUpdate((EntityPlayerSP) arg);
        }
        if (name.equals("onViewUpdate")) {
            onViewUpdate((EntityPlayer) arg);
        }
        if (name.equals("onServerUpdate")) {
            onServerUpdate((EntityPlayerMP) arg);
        }
        return arg;
    }

    @Override
    protected void init() {
        flyPlayer = null;
        flyDummy = null;
        playerNoclip = optNoclip;
        flyFree = optFreeFly;
        motionX = motionY = motionZ = 0;
    }

    @Override
    protected void quit() {
        flyPlayer = null;
        flyDummy  = null;
    }

    @Override
    protected void updateConfig() {
        tagFly              = getOptionString("tagFly");
        tagNoclip           = getOptionString("tagFlyNoClip");
        keyOn               = getOptionKey("keyFlyOn");
        keyOff              = getOptionKey("keyFlyOff");
        keyToggle           = getOptionKey("keyFlyToggle");
        keyUp               = getOptionKey("keyFlyUp");
        keyDown             = getOptionKey("keyFlyDown");
        keyForward          = 0;
        //keyForward          = getOptionKey("keyFlyForward");
        keyFreeFly          = getOptionKey("keyFlyFreeFly");
        keySpeed            = getOptionKey("keyFlySpeed");
        keyRun              = getOptionKey("keyFlyRun");
        keyNoclip           = getOptionKey("keyFlyNoClip");
        optAirJump          = getOptionBool("optFlyAirJump");
        optNoInertia        = getOptionBool("optFlyNoInertia");
        optFreeFly          = getOptionBool("optFlyFreeFly");
        optSpeedIsToggle    = getOptionBool("optFlySpeedIsToggle");
        optRunSpeedIsToggle = getOptionBool("optFlyRunSpeedIsToggle");
        optNoclip           = getOptionBool("optFlyNoClip");
        optVanillaFly       = getOptionBool("optFlyVanillaFly");
        optVanillaSprint    = getOptionBool("optFlyVanillaSprint");
        optJump             = getOptionFloat("optFlyJump");
        optJumpHigh         = getOptionFloat("optFlyJumpHigh");
        optSpeedVertical    = getOptionFloat("optFlySpeedVertical");
        optSpeedForward  = 0;
        //optSpeedForward     = getOptionFloat("optFlySpeedForward");
        optSpeedMulNormal   = getOptionFloat("optFlySpeedMulNormal");
        optSpeedMulModifier = getOptionFloat("optFlySpeedMulModifier");
        optRunSpeedMul      = getOptionFloat("optFlyRunSpeedMul");
        optRunSpeedVMul     = getOptionFloat("optFlyRunSpeedVMul");
    }

    @Override
    protected void onWorldChange() {
        flyPlayer = null;
        flyDummy  = null;
        modFlyAllowed = ZHandle.handle("allowFlying", true);
        modNoclipAllowed = ZHandle.handle("allowNoclip", true);
        playerNoclip = optNoclip && modNoclipAllowed;
        setNoclip(modFlyAllowed && playerNoclip && playerFly);
        flyFree = optFreeFly;
        motionX = motionY = motionZ = 0;
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        modFlyAllowed = ZHandle.handle("allowFlying", true);
        modNoclipAllowed = ZHandle.handle("allowNoclip", true);
        flyPlayer = player;
        controlledEntity = (Entity) ZHandle.handle("getControlledEntity", player);
        controllingPlayer = controlledEntity == player;
        if (controlledEntity instanceof DummyPlayer) {
            DummyPlayer controlledDummy = (DummyPlayer) controlledEntity;
            if (controlledDummy != flyDummy) {
                flyDummy = controlledDummy;
                dummyFly = getFlying(flyDummy) && playerFly;
                dummyNoclip = dummyFly && playerNoclip;
            }
        }
        flyRun = flySpeed = false;
        if (isInMenu()) {
            flyUp = flyDown = flyForward = false;
            return;
        }
        if (controllingPlayer) {
            boolean flyPrev = playerFly;
            if (wasKeyPressedThisTick(keyToggle)) {
                playerFly = !playerFly;
            } else if (isKeyDownThisTick(keyOn)) {
                playerFly = true;
            } else if (isKeyDownThisTick(keyOff)) {
                playerFly = false;
            }
            if (!modFlyAllowed && playerFly) {
                playerFly = false;
                chatClient("\u00a74zombe's \u00a72fly\u00a74-mod is not allowed on this server.");
            }
            if (flyPrev != playerFly) {
                setFlying(player, playerFly);
                if (player.capabilities.allowFlying) {
                    player.sendPlayerAbilities();
                }
            }
            if (playerFly && wasKeyPressedThisTick(keyNoclip)) {
                setNoclip(playerNoclip = !playerNoclip);
            } else if (flyPrev != playerFly) {
                setNoclip(playerFly && playerNoclip);
            }
            flySpeed = playerSpeed = optSpeedIsToggle && (wasKeyPressedThisTick(keySpeed) != playerSpeed);
            flyRun = playerRun = optRunSpeedIsToggle && (wasKeyPressedThisTick(keyRun) != playerRun);
            flying = playerFly;
            noclip = playerNoclip;
        } else {
            if (!optSpeedIsToggle) {
                playerSpeed = false;
            }
            if (!optRunSpeedIsToggle) {
                playerRun = false;
            }
        }
        if (!controllingPlayer) {
            boolean flyPrev = dummyFly;
            if (wasKeyPressedThisTick(keyToggle)) {
                dummyFly = !dummyFly;
            } else if (isKeyDownThisTick(keyOn)) {
                dummyFly = true;
            } else if (isKeyDownThisTick(keyOff)) {
                dummyFly = false;
            }
            if (flyPrev != dummyFly) {
                setFlying(flyDummy, dummyFly);
            }
            if (dummyFly && wasKeyPressedThisTick(keyNoclip)) {
                setNoclip(flyDummy, dummyNoclip = !dummyNoclip);
            } else if (flyPrev != dummyFly) {
                setNoclip(flyDummy, dummyFly && dummyNoclip);
            }
            flySpeed = dummySpeed = optSpeedIsToggle && (wasKeyPressedThisTick(keySpeed) != dummySpeed);
            flyRun = dummyRun = optRunSpeedIsToggle && (wasKeyPressedThisTick(keyRun) != dummyRun);
            flying = dummyFly;
            noclip = dummyNoclip;
        } else {
            if (!optSpeedIsToggle) {
                dummySpeed = false;
            }
            if (!optRunSpeedIsToggle) {
                dummyRun = false;
            }
        }
        if (wasKeyPressedThisTick(keyFreeFly)) {
            flyFree = !flyFree;
        }
        flyUp      = isKeyDownThisTick(keyUp);
        flyDown    = isKeyDownThisTick(keyDown);
        flyForward = isKeyDownThisTick(keyForward);
        flySpeed   = flySpeed || (!optSpeedIsToggle && isKeyDownThisTick(keySpeed));
        flyRun     = flyRun || (!optRunSpeedIsToggle && isKeyDownThisTick(keyRun));
    }

    @Override
    protected String getTag() {
        if (!playerFly) {
            return null;
        }
        String txt = tagFly;
        if (playerNoclip && tagNoclip.length() > 0) {
            txt += " " + tagNoclip;
        }
        return txt;
    }
}
