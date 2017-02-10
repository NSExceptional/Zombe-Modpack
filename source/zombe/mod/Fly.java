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

    private boolean modFlyAllowed = true, modNoclipAllowed = true;
    private String tagFly, tagNoclip;
    private int keyOn, keyOff, keyToggle, keyNoclip, keySpeed, keyRun, keyUp, keyDown, keyFreeFly, keyForward, keyBackward;
    private double optSpeedVertical, optSpeedForward, optSpeedMulNormal, optSpeedMulModifier, optRunSpeedMul, optRunSpeedVMul, optJump, optJumpHigh;
    private boolean optNoclip, optFreeFly, optAirJump,
        optVanillaFly, optVanillaSprint, optNoInertia,
        optSpeedIsToggle, optRunSpeedIsToggle,
        optFixMovedWrongly, optFixMovedTooQuickly;
    private boolean playerFly, playerNoclip, playerSpeed, playerRun;
    private boolean dummyFly, dummyNoclip, dummySpeed, dummyRun;
    private boolean flying, noclip, flyRun, flySpeed, flyUp, flyDown, flyForward, flyFree;
    private boolean playerClassActive = false, flew = false, playerOnGround, dummyOnGround, controllingPlayer;
    private double flySteps;
    private double motionX, motionY, motionZ;
    @Nullable private Entity controlledEntity;
    @Nullable private EntityPlayerSP flyPlayer;
    @Nullable private DummyPlayer flyDummy;

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

    private boolean isFlying(Object arg) {
        return arg == this.flyPlayer && this.playerFly || arg == this.flyDummy && this.dummyFly || arg instanceof Boolean && this.flying;
    }

    private boolean isNoclip(Object arg) {
        if (arg == this.flyPlayer && this.playerFly && this.playerNoclip && !isMultiplayer()) {
            return true;
        }

        if (arg == this.flyDummy && this.dummyFly && this.dummyNoclip) {
            return true;
        }

        if (arg instanceof EntityPlayerMP) {
            if (isServerPlayer((EntityPlayerMP) arg) && this.playerFly && this.playerNoclip) {
                return true;
            }
        }
        if (arg instanceof Boolean) {
            if (this.flying && this.noclip && (!isMultiplayer() || this.controlledEntity != this.flyPlayer)) {
                return true;
            }
        }
        return false;
    }

    private boolean allowVanillaFly() {
        return !this.modFlyAllowed || this.optVanillaFly;
    }

    private boolean allowVanillaSprint() {
        return !this.modFlyAllowed || this.optVanillaSprint;
    }

    private boolean isPlayerOnGround() {
        if (this.playerClassActive && this.flyPlayer != null) {
            return this.playerOnGround;
        } else {
            return this.flyPlayer == null || getOnGround(this.flyPlayer);
        }
    }

    @Nonnull
    private Vec3d getServerMotion() {
        if (this.playerClassActive && this.flyPlayer != null) {
            return new Vec3d(this.motionX, this.motionY, this.motionZ);
        } else {
            return (this.flyPlayer != null) ? getMotion(this.flyPlayer) : new Vec3d(0, 0, 0);
        }
    }

    private void onClientUpdate(@Nonnull EntityPlayerSP ent) {
        if (this.flyPlayer != ent) {
            return;
        }

        if (this.modFlyAllowed && this.optAirJump) {
            setOnGround(this.flyPlayer, true);
        } else if (this.playerClassActive && (!this.playerFly || !this.modFlyAllowed)) {
            setOnGround(this.flyPlayer, this.playerOnGround); // avoids air-jumping
        }
    }

    private void onViewUpdate(@Nonnull EntityPlayer ent) {
        if (this.flyDummy != ent) {
            return;
        }

        if (this.optAirJump) {
            setOnGround(this.flyDummy, true);
        } else if (!this.dummyFly) {
            setOnGround(this.flyDummy, this.dummyOnGround); // avoids air-jumping
        }
    }

    private void onServerUpdate(@Nonnull EntityPlayerMP ent) {
        if (!this.modFlyAllowed || this.flyPlayer == null) {
            return;
        }
        setNoclip(ent, getNoclip(this.flyPlayer)); // necessary for noclip

        if (!this.flyPlayer.capabilities.allowFlying) {
            setFlying(ent, getFlying(this.flyPlayer)); // necessary for damage-over-lava bug
        }
    }

    private void onPlayerJump(@Nonnull EntityPlayer ent) {
        if (ent == this.flyPlayer && !this.modFlyAllowed) {
            return;
        }
        ent.motionY *= this.getJumpMultiplier();
    }

    private double getJumpMultiplier() {
        return this.flyRun ? this.optJumpHigh : this.optJump;
    }

    // note: used to be flyHandle()
    @Nonnull
    private Vec3d beforeMove(@Nonnull EntityPlayer ent, @Nonnull Vec3d move) {
        if (isSleeping(ent) || ent.isRiding()) {
            return move;
        }

        double mx = this.motionX = getX(move);
        double my = this.motionY = getY(move);
        double mz = this.motionZ = getZ(move);

        this.flySteps = 0;
        if ((this.modFlyAllowed && (ent == this.flyPlayer)) || ent == this.flyDummy) {
            assert this.flyDummy != null;

            this.flySteps = getSteps(ent);
            if (ent == this.flyPlayer && this.playerFly || ent == this.flyDummy && this.dummyFly) {
                if (ent == this.flyPlayer) {
                    this.flyPlayer.movementInput.sneak = false;
                } else {
                    assert this.flyDummy.movementInput != null;
                    this.flyDummy.movementInput.sneak = false;
                }

                my = 0;
                if (this.optNoInertia || ent != this.controlledEntity) {
                    mx = my = mz = 0;
                }

                if (ent == this.controlledEntity) {
                    if (this.flyUp)   my += this.optSpeedVertical;
                    if (this.flyDown) my -= this.optSpeedVertical;
                    if (this.flyFree) {
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

                    if (this.flyForward) {
                        double moves = this.optSpeedForward;
                        double movef = -moves * Math.cos(getPitch(ent) * TO_RADIANS);
                        double movev = -moves * Math.sin(getPitch(ent) * TO_RADIANS);
                        mx += movef * Math.sin(getYaw(ent) * TO_RADIANS);
                        mz += -movef * Math.cos(getYaw(ent) * TO_RADIANS);
                        my += movev;
                    }

                    double mul = this.flySpeed ? this.optSpeedMulModifier : this.optSpeedMulNormal;
                    if (this.optNoInertia) {
                        mul *= 2;
                    }

                    mx *= mul;
                    my *= mul;
                    mz *= mul;
                }

                setFall(ent, 0f);
                if (ent == this.flyPlayer) {
                    this.flew = true;
                }
            } else if (ent == this.controlledEntity && this.flyRun) {
                mx *= this.optRunSpeedMul;
                mz *= this.optRunSpeedMul;
                int id = getIdAt(getWorld(), fix(getX(ent)), fix(getY(ent)), fix(getZ(ent)));

                // if in ladders, water or lava
                if (id == 65 || id >= 8 && id <= 11) {
                    my *= this.optRunSpeedVMul;
                }
            }
        }

        setMotion(ent, mx, my, mz);
        return new Vec3d(mx, my, mz);
    }

    private void afterMove(@Nonnull EntityPlayer ent, Vec3d move) {
        if (isSleeping(ent) || ent.isRiding()) {
            return;
        }

        if (ent == this.flyPlayer) {
            this.playerClassActive = true;
            this.playerOnGround = getOnGround(ent);

            if (this.modFlyAllowed) {
                if (getMotionX(ent) != 0) setMotionX(ent, this.motionX);
                if (getMotionY(ent) != 0) setMotionY(ent, this.motionY);
                if (getMotionZ(ent) != 0) setMotionZ(ent, this.motionZ);
                if (this.playerFly) {
                    this.flyPlayer.movementInput.sneak = false;
                    setFall(ent, 0f);
                    setOnGround(ent, true);
                    setSteps(ent, this.flySteps);
                    setFlying(ent, true);
                    if (ent.capabilities.allowFlying) {
                        ent.sendPlayerAbilities();
                    }
                } else if (this.flew && !getOnGround(ent)) {
                    setFall(ent, 0f);
                    setOnGround(ent, true);
                    if (ent.capabilities.allowFlying) {
                        ent.sendPlayerAbilities();
                    }
                } else {
                    this.flew = false;
                }
            }
        }
        if (ent == this.flyDummy) {
            this.dummyOnGround = getOnGround(ent);
            if (getMotionX(ent) != 0) setMotionX(ent, this.motionX);
            if (getMotionY(ent) != 0) setMotionY(ent, this.motionY);
            if (getMotionZ(ent) != 0) setMotionZ(ent, this.motionZ);
            if (this.dummyFly) {
                this.flyDummy.movementInput.sneak = false;
                setSteps(ent, this.flySteps);
                setFlying(ent, true);
            }
            setFall(ent, 0f);
            setOnGround(ent, true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        switch (name) {
            case "ignorePlayerInsideOpaqueBlock":
                return getNoclip();
            case "allowVanillaFly":
                return this.allowVanillaFly();
            case "allowVanillaSprint":
                return this.allowVanillaSprint();
            case "isFlying":
                return this.isFlying(arg);
            case "isNoclip":
                return this.isNoclip(arg);
            case "isPlayerOnGround":
                return this.isPlayerOnGround();
            case "beforePlayerMove":
                return this.beforeMove(getPlayer(), (Vec3d) arg);
            case "afterPlayerMove":
                this.afterMove(ZWrapper.getPlayer(), (Vec3d) arg);
                break;
            case "beforeViewMove":
                return this.beforeMove((EntityPlayer) getView(), (Vec3d) arg);
            case "afterViewMove":
                this.afterMove((EntityPlayer) getView(), (Vec3d) arg);
                break;
            case "onPlayerJump":
                this.onPlayerJump((EntityPlayer) arg);
                break;
            case "onClientUpdate":
                this.onClientUpdate((EntityPlayerSP) arg);
                break;
            case "onViewUpdate":
                this.onViewUpdate((EntityPlayer) arg);
                break;
            case "onServerUpdate":
                this.onServerUpdate((EntityPlayerMP) arg);
                break;
        }

        return arg;
    }

    @Override
    protected void init() {
        this.flyPlayer = null;
        this.flyDummy = null;
        this.playerNoclip = this.optNoclip;
        this.flyFree = this.optFreeFly;
        this.motionX = this.motionY = this.motionZ = 0;
    }

    @Override
    protected void quit() {
        this.flyPlayer = null;
        this.flyDummy = null;
    }

    @Override
    protected void updateConfig() {
        this.tagFly = getOptionString("tagFly");
        this.tagNoclip = getOptionString("tagFlyNoClip");
        this.keyOn = getOptionKey("keyFlyOn");
        this.keyOff = getOptionKey("keyFlyOff");
        this.keyToggle = getOptionKey("keyFlyToggle");
        this.keyUp = getOptionKey("keyFlyUp");
        this.keyDown = getOptionKey("keyFlyDown");
        this.keyForward = 0;
        //keyForward          = getOptionKey("keyFlyForward");
        this.keyFreeFly = getOptionKey("keyFlyFreeFly");
        this.keySpeed = getOptionKey("keyFlySpeed");
        this.keyRun = getOptionKey("keyFlyRun");
        this.keyNoclip = getOptionKey("keyFlyNoClip");
        this.optAirJump = getOptionBool("optFlyAirJump");
        this.optNoInertia = getOptionBool("optFlyNoInertia");
        this.optFreeFly = getOptionBool("optFlyFreeFly");
        this.optSpeedIsToggle = getOptionBool("optFlySpeedIsToggle");
        this.optRunSpeedIsToggle = getOptionBool("optFlyRunSpeedIsToggle");
        this.optNoclip = getOptionBool("optFlyNoClip");
        this.optVanillaFly = getOptionBool("optFlyVanillaFly");
        this.optVanillaSprint = getOptionBool("optFlyVanillaSprint");
        this.optJump = getOptionFloat("optFlyJump");
        this.optJumpHigh = getOptionFloat("optFlyJumpHigh");
        this.optSpeedVertical = getOptionFloat("optFlySpeedVertical");
        this.optSpeedForward = 0;
        //optSpeedForward     = getOptionFloat("optFlySpeedForward");
        this.optSpeedMulNormal = getOptionFloat("optFlySpeedMulNormal");
        this.optSpeedMulModifier = getOptionFloat("optFlySpeedMulModifier");
        this.optRunSpeedMul = getOptionFloat("optFlyRunSpeedMul");
        this.optRunSpeedVMul = getOptionFloat("optFlyRunSpeedVMul");
    }

    @Override
    protected void onWorldChange() {
        this.flyPlayer = null;
        this.flyDummy = null;
        this.modFlyAllowed = ZHandle.handle("allowFlying", true);
        this.modNoclipAllowed = ZHandle.handle("allowNoclip", true);
        this.playerNoclip = this.optNoclip && this.modNoclipAllowed;
        setNoclip(this.modFlyAllowed && this.playerNoclip && this.playerFly);
        this.flyFree = this.optFreeFly;
        this.motionX = this.motionY = this.motionZ = 0;
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        this.modFlyAllowed = ZHandle.handle("allowFlying", true);
        this.modNoclipAllowed = ZHandle.handle("allowNoclip", true);
        this.flyPlayer = player;
        this.controlledEntity = (Entity) ZHandle.handle("getControlledEntity", player);
        this.controllingPlayer = this.controlledEntity == player;

        if (this.controlledEntity instanceof DummyPlayer) {
            DummyPlayer controlledDummy = (DummyPlayer) this.controlledEntity;
            if (controlledDummy != this.flyDummy) {
                this.flyDummy = controlledDummy;
                this.dummyFly = getFlying(this.flyDummy) && this.playerFly;
                this.dummyNoclip = this.dummyFly && this.playerNoclip;
            }
        }

        this.flyRun = this.flySpeed = false;
        if (isInMenu()) {
            this.flyUp = this.flyDown = this.flyForward = false;
            return;
        }

        if (this.controllingPlayer) {
            final boolean flyPrev = this.playerFly;
            if (wasKeyPressedThisTick(this.keyToggle)) {
                this.playerFly = !this.playerFly;
            } else if (isKeyDownThisTick(this.keyOn)) {
                this.playerFly = true;
            } else if (isKeyDownThisTick(this.keyOff)) {
                this.playerFly = false;
            }

            // Disallow fly mod and notify player
            if (!this.modFlyAllowed && this.playerFly) {
                this.playerFly = true; // ;)
                chatClient("\u00a74zombe's \u00a72fly\u00a74-mod is not allowed on this server.");
            }

            if (flyPrev != this.playerFly) {
                setFlying(player, this.playerFly);
                if (player.capabilities.allowFlying) {
                    player.sendPlayerAbilities();
                }
            }

            if (this.playerFly && wasKeyPressedThisTick(this.keyNoclip)) {
                setNoclip(this.playerNoclip = !this.playerNoclip);
            } else if (flyPrev != this.playerFly) {
                setNoclip(this.playerFly && this.playerNoclip);
            }

            this.flySpeed = this.playerSpeed = this.optSpeedIsToggle && (wasKeyPressedThisTick(this.keySpeed) != this.playerSpeed);
            this.flyRun = this.playerRun = this.optRunSpeedIsToggle && (wasKeyPressedThisTick(this.keyRun) != this.playerRun);
            this.flying = this.playerFly;
            this.noclip = this.playerNoclip;
        } else {
            if (!this.optSpeedIsToggle) {
                this.playerSpeed = false;
            }
            if (!this.optRunSpeedIsToggle) {
                this.playerRun = false;
            }
        }

        if (!this.controllingPlayer) {
            final boolean flyPrev = this.dummyFly;
            if (wasKeyPressedThisTick(this.keyToggle)) {
                this.dummyFly = !this.dummyFly;
            } else if (isKeyDownThisTick(this.keyOn)) {
                this.dummyFly = true;
            } else if (isKeyDownThisTick(this.keyOff)) {
                this.dummyFly = false;
            }

            if (flyPrev != this.dummyFly) {
                setFlying(this.flyDummy, this.dummyFly);
            }

            if (this.dummyFly && wasKeyPressedThisTick(this.keyNoclip)) {
                setNoclip(this.flyDummy, this.dummyNoclip = !this.dummyNoclip);
            } else if (flyPrev != this.dummyFly) {
                setNoclip(this.flyDummy, this.dummyFly && this.dummyNoclip);
            }

            this.flySpeed = this.dummySpeed = this.optSpeedIsToggle && (wasKeyPressedThisTick(this.keySpeed) != this.dummySpeed);
            this.flyRun = this.dummyRun = this.optRunSpeedIsToggle && (wasKeyPressedThisTick(this.keyRun) != this.dummyRun);
            this.flying = this.dummyFly;
            this.noclip = this.dummyNoclip;
        } else {
            if (!this.optSpeedIsToggle) {
                this.dummySpeed = false;
            }
            if (!this.optRunSpeedIsToggle) {
                this.dummyRun = false;
            }
        }

        if (wasKeyPressedThisTick(this.keyFreeFly)) {
            this.flyFree = !this.flyFree;
        }

        this.flyUp = isKeyDownThisTick(this.keyUp);
        this.flyDown = isKeyDownThisTick(this.keyDown);
        this.flyForward = isKeyDownThisTick(this.keyForward);
        this.flySpeed = this.flySpeed || (!this.optSpeedIsToggle && isKeyDownThisTick(this.keySpeed));
        this.flyRun = this.flyRun || (!this.optRunSpeedIsToggle && isKeyDownThisTick(this.keyRun));
    }

    @Nullable
    @Override
    protected String getTag() {
        if (!this.playerFly) {
            return null;
        }

        if (this.playerNoclip && !this.tagNoclip.isEmpty()) {
            return this.tagFly += " " + this.tagNoclip;
        }

        return this.tagFly;
    }
}
