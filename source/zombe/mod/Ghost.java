package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;
import zombe.core.ZWrapper;
import zombe.core.content.DummyPlayer;
import zombe.core.util.Orientation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static zombe.core.ZWrapper.*;

public final class Ghost extends ZMod {

    private static int keyPossession, keyProjection, keyRelocate;
    private static boolean optProjectionSwing, optProjectionSpoof, optProjectionPlace, optResetOnSendMotion, optResetOnUpdateRenderer, optResetOnGetMouseOver;

    private static float ghostYaw, ghostPitch;
    @Nullable private static EntityLivingBase ghostView;
    @Nullable private static EntityLivingBase ghostPossession;
    @Nullable private static EntityPlayerSP ghostPlayer;
    @Nullable private static DummyPlayer    ghostProjection;
    private static boolean ghostProjectionLock, ghostUnspoof;
    @Nullable private static MovementInput playerMovementInput;

    private static boolean doResetOnUpdateRenderer, doResetOnGetMouseOver;

    public Ghost() {
        super("ghost", "1.8", "9.0.0");
        this.registerHandler("onSetAngles");
        this.registerHandler("allowSwing");
        this.registerHandler("allowItemSync");
        this.registerHandler("isControllingPlayer");
        this.registerHandler("isControllingView");
        this.registerHandler("getControlledEntity");
        this.registerHandler("shouldUpdatePlayerActionState");
        this.registerListener("onClientUpdate");
        this.registerListener("beforeSendMotion");
        this.registerListener("afterSendMotion");
        this.registerListener("beforeUpdateRenderer");
        this.registerListener("catchUpdateRenderer");
        this.registerListener("afterUpdateRenderer");
        this.registerListener("beforeGetMouseOver");
        this.registerListener("catchGetMouseOver");
        this.registerListener("afterGetMouseOver");

        this.addOption("keyGhostPossession", "Toggle target player's view", Keyboard.KEY_NUMPAD5);
        this.addOption("keyGhostProjection", "Toggle astral projection", Keyboard.KEY_NUMPAD8);
        this.addOption("keyGhostRelocate", "Relocate projection to current view", Keyboard.KEY_NUMPAD2);
        this.addOption("optGhostProjectionSwing", "(Experimental) hide arm swing", true);
        this.addOption("optGhostProjectionSpoof", "(Experimental) hide active item in hand", false);
        this.addOption("optGhostProjectionPlace", "(Experimental) hide orientation", false);
        this.addOption("compatibility switches");
        this.addOption("optGhostResetOnSendMotion", "Temporarily reset view on sendMotion", true);
        this.addOption("optGhostResetOnUpdateRenderer", "Temporarily reset view on updateRenderer", false);
        this.addOption("optGhostResetOnGetMouseOver", "Temporarily reset view on getMouseOver", false);
    }











    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name == "isControllingPlayer") {
            return isControllingPlayer();
        }
        if (name == "isControllingView") {
            return isControllingView();
        }
        if (name == "getControlledEntity") {
            return getControlledEntity();
        }
        if (name == "shouldUpdatePlayerActionState") {
            return shouldUpdatePlayerActionState();
        }
        if (name == "onSetAngles") {
            return onSetAngles((Orientation) arg);
        }
        if (name == "allowSwing") {
            return allowSwing();
        }
        if (name == "allowItemSync") {
            return allowItemSync();
        }
        if (name == "onClientUpdate") {
            onClientUpdate();
        }
        if (name == "beforeSendMotion") {
            beforeReset(optResetOnSendMotion);
        }
        if (name == "afterSendMotion") {
            afterReset(optResetOnSendMotion);
        }
        if (name == "beforeUpdateRenderer") {
            beforeReset(doResetOnUpdateRenderer);
        }
        if (name == "afterUpdateRenderer") {
            afterReset(doResetOnUpdateRenderer);
        }
        if (name == "catchUpdateRenderer") {
            doResetOnUpdateRenderer = true;
        }
        if (name == "beforeGetMouseOver") {
            beforeReset(doResetOnGetMouseOver);
        }
        if (name == "afterGetMouseOver") {
            afterReset(doResetOnGetMouseOver);
        }
        if (name == "catchGetMouseOver") {
            doResetOnGetMouseOver = true;
        }
        return arg;
    }

    private static void beforeReset(boolean optReset) {
        if (optReset && ghostView != null) {
            setView(ghostPlayer);
        }
    }

    private static void afterReset(boolean optReset) {
        if (optReset && ghostView != null) {
            setView(ghostView);
        }
    }

    private static void onClientUpdate() {
        if (ghostProjection != null && getView() == ghostProjection) {
            ghostProjection.onUpdate();
        }
    }

    private static boolean shouldUpdatePlayerActionState() {
        return getView() == getPlayer() || getView() == ghostView;
    }

    private static boolean isControllingPlayer() {
        return !isControllingProjection();
    }

    private static boolean isControllingProjection() {
        return getView() == ghostProjection && !ghostProjectionLock;
    }

    private static boolean isControllingView() {
        return getView() == getPlayer() || isControllingProjection();
    }

    @Nullable
    private static EntityPlayer getControlledEntity() {
        return isControllingProjection() ? ghostProjection : getPlayer();
    }

    private static boolean allowSwing() {
        return isControllingPlayer() || optProjectionSwing;
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name == "isControllingPlayer") {
            return isControllingPlayer();
        }
        if (name == "isControllingView") {
            return isControllingView();
        }
        if (name == "getControlledEntity") {
            return getControlledEntity();
        }
        if (name == "shouldUpdatePlayerActionState") {
            return shouldUpdatePlayerActionState();
        }
        if (name == "onSetAngles") {
            return onSetAngles((Orientation) arg);
        }
        if (name == "allowSwing") {
            return allowSwing();
        }
        if (name == "allowItemSync") {
            return allowItemSync();
        }
        if (name == "onClientUpdate") {
            onClientUpdate();
        }
        if (name == "beforeSendMotion") {
            beforeReset(optResetOnSendMotion);
        }
        if (name == "afterSendMotion") {
            afterReset(optResetOnSendMotion);
        }
        if (name == "beforeUpdateRenderer") {
            beforeReset(doResetOnUpdateRenderer);
        }
        if (name == "afterUpdateRenderer") {
            afterReset(doResetOnUpdateRenderer);
        }
        if (name == "catchUpdateRenderer") {
            doResetOnUpdateRenderer = true;
        }
        if (name == "beforeGetMouseOver") {
            beforeReset(doResetOnGetMouseOver);
        }
        if (name == "afterGetMouseOver") {
            afterReset(doResetOnGetMouseOver);
        }
        if (name == "catchGetMouseOver") {
            doResetOnGetMouseOver = true;
        }
        return arg;
    }    private static boolean allowItemSync() {
        return isControllingPlayer() || !optProjectionSpoof;
    }

    @Override
    protected void init() {
        ghostView = null;
        ghostPlayer = null;
        ghostPossession = null;
        ghostProjection = null;
        ghostProjectionLock = false;
        ghostUnspoof = false;
        playerMovementInput = null;
    }    @Nonnull
    private static Orientation onSetAngles(@Nonnull Orientation rot) {
        if (getPlayer() == null || isControllingPlayer()) {
            return rot;
        }
        ghostProjection.setAngles(rot.yaw, rot.pitch);
        return new Orientation(0, 0);
    }

    @Override
    protected void quit() {
        setView(getPlayer());
        ghostView = null;
        ghostPlayer = null;
        ghostPossession = null;
        ghostProjection = null;
        playerMovementInput = null;
        setMessage("view", null);
    }

    private static void beforeBlockDig() {
        if (isControllingProjection() && optProjectionSpoof) {
            if (!ghostUnspoof) {
                getPlayerController().switchToRealItem();
            }
            ghostUnspoof = true;
        }
    }

    @Override
    protected void updateConfig() {
        keyPossession = getOptionKey("keyGhostPossession");
        keyProjection = getOptionKey("keyGhostProjection");
        keyRelocate = getOptionKey("keyGhostRelocate");
        optProjectionSwing = getOptionBool("optGhostProjectionSwing");
        optProjectionSpoof = getOptionBool("optGhostProjectionSpoof");
        optProjectionPlace = getOptionBool("optGhostProjectionPlace");
        optResetOnSendMotion = getOptionBool("optGhostResetOnSendMotion");
        doResetOnUpdateRenderer = optResetOnUpdateRenderer = getOptionBool("optGhostResetOnUpdateRenderer");
        doResetOnGetMouseOver = optResetOnGetMouseOver = getOptionBool("optGhostResetOnGetMouseOver");
    }    private static void afterBlockDig() {
        if (isControllingProjection() && optProjectionSpoof) {
            if (ghostUnspoof) {
                getPlayerController().switchToIdleItem();
            }
            ghostUnspoof = false;
        }
    }

    @Override
    protected void onWorldChange() {
        if (playerMovementInput != null && ghostPlayer != null) {
            ghostPlayer.movementInput = playerMovementInput;
        }
        if (ghostView != null && getView() == ghostView) {
            setView(getPlayer());
        }
        ghostView = null;
        ghostPlayer = null;
        ghostPossession = null;
        ghostProjection = null;
        playerMovementInput = null;
    }    private static void beforeBlockPlace() {
        if (isControllingProjection() && optProjectionSpoof) {
            if (!ghostUnspoof) {
                getPlayerController().switchToRealItem();
            }
            ghostUnspoof = true;
        }
        if (isControllingProjection() && optProjectionPlace) {
            ghostYaw = getYaw(ghostPlayer);
            ghostPitch = getPitch(ghostPlayer);
            setYaw(ghostPlayer, getYaw(ghostProjection));
            setPitch(ghostPlayer, getPitch(ghostProjection));
            sendMotionUpdates(ghostPlayer);
        }
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        if (player != ghostPlayer) {
            ghostPlayer = player;
            playerMovementInput = player.movementInput;
            if (ghostProjection != null) {
                if (ghostProjection == ghostView) {
                    ghostView = null;
                    setView(player);
                }
                ghostProjection = null;
                ghostProjectionLock = false;
            }
        }
        List list = ZWrapper.getEntities();
        if (ghostView != null) {
            if (getView() == player) {
                if (ghostView == ghostProjection && !ghostProjectionLock) {
                    ghostProjection.movementInput = new MovementInput();
                    player.movementInput = playerMovementInput;
                    setView(player);
                    if (optProjectionSpoof) {
                        setView(player);
                        syncCurrentItem();
                    }
                }
                ghostView = null;
            }
            if (ghostView == null) {
                setView(player);
            }
        }
        if (ghostPossession != null) {
            if (!list.contains(ghostPossession) || ghostView != ghostPossession) {
                if (ghostView == ghostPossession) {
                    ghostView = null;
                }
                ghostPossession = null;
            }
            if (ghostView == null) {
                setView(player);
            }
        }
        if (ghostProjection != null) {
            if (!list.contains(ghostProjection.playerBody)) {
                if (ghostView == ghostProjection && !ghostProjectionLock) {
                    ghostProjection.movementInput = new MovementInput();
                    player.movementInput = playerMovementInput;
                    if (optProjectionSpoof) {
                        setView(player);
                        syncCurrentItem();
                    }
                }
                if (ghostView == ghostProjection) {
                    ghostView = null;
                }
                ghostProjection = null;
            }
            if (ghostView == null) {
                setView(player);
            }
        }
        if (ghostView != null) {
            String message = "View: \u00a7b" + ZWrapper.getName(ghostView) + "\u00a7f";
            if (getView() == ghostProjection) {
                if (getFlying(ghostProjection)) {
                    message += " flying";
                }
                if (getNoclip(ghostProjection)) {
                    message += " noclip";
                }
            }
            setMessage("view", message);
        } else {
            setMessage("view", null);
        }

        if (!isInMenu()) {
            if (wasKeyPressedThisTick(keyPossession)) {
                if (ghostPossession != null) {
                    ghostPossession = null;
                    ghostView = null;
                    setView(player);
                } else {
                    Entity eye = getView();
                    // hold on to your hats - math follows
                    double x1, x2, x3, xt, y1, y2, y3, yt, z1, z2, z3, zt, yaw, pitch, u, distS, factor;
                    x1 = getX(eye);
                    y1 = getY(eye);
                    z1 = getZ(eye);
                    yaw = getYaw(eye) * (Math.PI / 180.0);
                    pitch = getPitch(eye) * (Math.PI / 180.0);
                    x2 = x1 + 100f * (-Math.sin(yaw) * Math.abs(Math.cos(pitch)));
                    y2 = y1 + 100f * (-Math.sin(pitch));
                    z2 = z1 + 100f * (Math.cos(yaw) * Math.abs(Math.cos(pitch)));
                    EntityLivingBase best = null;
                    double bestDS = 1000000000f;
                    for (Object obj : list) {
                        if (!(obj instanceof EntityLivingBase) || obj == eye) {
                            continue;
                        }
                        EntityLivingBase ent = (EntityLivingBase) obj;
                        if (!(ent instanceof EntityPlayer)) {
                            continue; /* can not view from other mobs for now */
                        }
                        x3 = getX(ent);
                        y3 = getY(ent);
                        z3 = getZ(ent);
                        if ((x2 - x1) * (x3 - x1) + (y2 - y1) * (y3 - y1) + (z2 - z1) * (z3 - z1) < 0f) {
                            continue;
                        }
                        factor = 1f / ((x1 - x3) * (x1 - x3) + (y1 - y3) * (y1 - y3) + (z1 - z3) * (z1 - z3));
                        xt = x2 - x1;
                        yt = y2 - y1;
                        zt = z2 - z1;
                        u = xt * xt + yt * yt + zt * zt;
                        u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1) + (z3 - z1) * (z2 - z1)) / u;
                        xt = x1 + u * (x2 - x1) - x3;
                        yt = y1 + u * (y2 - y1) - y3;
                        zt = z1 + u * (z2 - z1) - z3;
                        distS = (xt * xt + yt * yt + zt * zt) * factor;
                        if (distS < bestDS) {
                            best = ent;
                            bestDS = distS;
                        }
                    }
                    if (best != null) {
                        if (getView() == ghostProjection && !ghostProjectionLock) {
                            ghostProjection.movementInput = new MovementInput();
                            player.movementInput = playerMovementInput;
                            if (optProjectionSpoof) {
                                setView(player);
                                syncCurrentItem();
                            }
                        }
                        setView(best);
                        if (best == player) {
                            best = null;
                        }
                        ghostView = ghostPossession = best;
                    }
                }
            }
            if (wasKeyPressedThisTick(keyProjection)) {
                if (ghostProjection == null) {
                    ghostProjection = new DummyPlayer(player);
                    ghostProjection.placeAt(getView());
                    if (getView() == player) {
                        setFlying(ghostProjection, getFlying(player));
                        setNoclip(ghostProjection, getNoclip(player));
                    }
                }
                if (ghostView == ghostProjection) {
                    ghostView = null;
                    if (!ghostProjectionLock) {
                        player.movementInput = playerMovementInput;
                        ghostProjection.movementInput = new MovementInput();
                        if (optProjectionSpoof) {
                            setView(player);
                            syncCurrentItem();
                        }
                    }
                } else {
                    ghostView = ghostProjection;
                    if (!ghostProjectionLock) {
                        player.movementInput = new MovementInput();
                        ghostProjection.movementInput = playerMovementInput;
                    }
                }
                if (ghostView != null) {
                    setView(ghostView);
                } else {
                    setView(player);
                }
            }
            if (wasKeyPressedThisTick(keyRelocate)) {
                //if (ghostProjection == null) {
                if (true) {
                    boolean was = getView() == ghostProjection;
                    ghostProjection = new DummyPlayer(player);
                    if (was) {
                        setView(ghostView = ghostProjection);
                    }
                    if (!was || ghostProjectionLock) {
                        ghostProjection.movementInput = new MovementInput();
                    } else {
                        ghostProjection.movementInput = playerMovementInput;
                    }
                    if (!was) {
                        setFlying(ghostProjection, getFlying((EntityPlayer) getView()));
                    } else if (ghostProjectionLock) {
                        setFlying(ghostProjection, getFlying(player));
                    }
                }
                ghostProjection.placeAt(ghostPossession != null ? ghostPossession : player);
            }
        }
    }    private static void afterBlockPlace() {
        if (isControllingProjection() && optProjectionSpoof) {
            if (ghostUnspoof) {
                getPlayerController().switchToIdleItem();
            }
            ghostUnspoof = false;
        }
        if (isControllingProjection() && optProjectionPlace) {
            setYaw(ghostPlayer, ghostYaw);
            setPitch(ghostPlayer, ghostPitch);
            sendMotionUpdates(ghostPlayer);
        }
    }
}
