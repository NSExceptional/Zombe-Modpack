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

    private int keyPossession, keyProjection, keyRelocate;
    private double ghostYaw, ghostPitch;
    private boolean optProjectionSwing, optProjectionSpoof, optProjectionPlace,
                    optResetOnSendMotion, optResetOnUpdateRenderer, optResetOnGetMouseOver;
    private boolean ghostProjectionLock, ghostUnspoof;
    private boolean doResetOnUpdateRenderer, doResetOnGetMouseOver;
    @Nullable private EntityLivingBase ghostView;
    @Nullable private EntityLivingBase ghostPossession;
    @Nullable private EntityPlayerSP ghostPlayer;
    @Nullable private DummyPlayer ghostProjection;
    @Nullable private MovementInput playerMovementInput;

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

    private void afterBlockPlace() {
        if (this.isControllingProjection() && this.optProjectionSpoof) {
            if (this.ghostUnspoof) {
                getPlayerController().switchToIdleItem();
            }
            this.ghostUnspoof = false;
        }
        if (this.isControllingProjection() && this.optProjectionPlace) {
            assert this.ghostPlayer != null;

            setYaw(this.ghostPlayer, this.ghostYaw);
            setPitch(this.ghostPlayer, this.ghostPitch);
            sendMotionUpdates(this.ghostPlayer);
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        switch (name) {
            case "isControllingPlayer":
                return this.isControllingPlayer();
            case "isControllingView":
                return this.isControllingView();
            case "getControlledEntity":
                return this.getControlledEntity();
            case "shouldUpdatePlayerActionState":
                return this.shouldUpdatePlayerActionState();
            case "onSetAngles":
                assert arg instanceof Orientation;
                return this.onSetAngles((Orientation) arg);
            case "allowSwing":
                return this.allowSwing();
            case "allowItemSync":
                return this.allowItemSync();
            case "onClientUpdate":
                this.onClientUpdate();
                break;
            case "beforeSendMotion":
                this.beforeReset(this.optResetOnSendMotion);
                break;
            case "afterSendMotion":
                this.afterReset(this.optResetOnSendMotion);
                break;
            case "beforeUpdateRenderer":
                this.beforeReset(this.doResetOnUpdateRenderer);
                break;
            case "afterUpdateRenderer":
                this.afterReset(this.doResetOnUpdateRenderer);
                break;
            case "catchUpdateRenderer":
                this.doResetOnUpdateRenderer = true;
                break;
            case "beforeGetMouseOver":
                this.beforeReset(this.doResetOnGetMouseOver);
                break;
            case "afterGetMouseOver":
                this.afterReset(this.doResetOnGetMouseOver);
                break;
            case "catchGetMouseOver":
                this.doResetOnGetMouseOver = true;
                break;
        }

        return arg;
    }

    @Override
    protected void init() {
        this.ghostView = null;
        this.ghostPlayer = null;
        this.ghostPossession = null;
        this.ghostProjection = null;
        this.ghostProjectionLock = false;
        this.ghostUnspoof = false;
        this.playerMovementInput = null;
    }

    @Override
    protected void quit() {
        setView(getPlayer());
        this.ghostView = null;
        this.ghostPlayer = null;
        this.ghostPossession = null;
        this.ghostProjection = null;
        this.playerMovementInput = null;
        setMessage("view", null);
    }

    @Override
    protected void updateConfig() {
        this.keyPossession = getOptionKey("keyGhostPossession");
        this.keyProjection = getOptionKey("keyGhostProjection");
        this.keyRelocate = getOptionKey("keyGhostRelocate");
        this.optProjectionSwing = getOptionBool("optGhostProjectionSwing");
        this.optProjectionSpoof = getOptionBool("optGhostProjectionSpoof");
        this.optProjectionPlace = getOptionBool("optGhostProjectionPlace");
        this.optResetOnSendMotion = getOptionBool("optGhostResetOnSendMotion");
        this.doResetOnUpdateRenderer = this.optResetOnUpdateRenderer = getOptionBool("optGhostResetOnUpdateRenderer");
        this.doResetOnGetMouseOver = this.optResetOnGetMouseOver = getOptionBool("optGhostResetOnGetMouseOver");
    }

    @Override
    protected void onWorldChange() {
        if (this.playerMovementInput != null && this.ghostPlayer != null) {
            this.ghostPlayer.movementInput = this.playerMovementInput;
        }
        if (this.ghostView != null && getView() == this.ghostView) {
            setView(getPlayer());
        }

        this.ghostView = null;
        this.ghostPlayer = null;
        this.ghostPossession = null;
        this.ghostProjection = null;
        this.playerMovementInput = null;
    }

    // TODO split into mulitple methods
    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        if (player != this.ghostPlayer) {
            this.ghostPlayer = player;
            this.playerMovementInput = player.movementInput;
            if (this.ghostProjection != null) {
                if (this.ghostProjection == this.ghostView) {
                    this.ghostView = null;
                    setView(player);
                }
                this.ghostProjection = null;
                this.ghostProjectionLock = false;
            }
        }

        final List<Entity> list = ZWrapper.getEntities();
        if (this.ghostView != null) {
            if (getView() == player) {
                if (this.ghostView == this.ghostProjection && !this.ghostProjectionLock) {
                    this.ghostProjection.movementInput = new MovementInput();
                    player.movementInput = this.playerMovementInput;
                    setView(player);

                    if (this.optProjectionSpoof) {
                        setView(player);
                        syncCurrentItem();
                    }
                }

                this.ghostView = null;
            }

            if (this.ghostView == null) {
                setView(player);
            }
        }

        if (this.ghostPossession != null) {
            if (!list.contains(this.ghostPossession) || this.ghostView != this.ghostPossession) {
                if (this.ghostView == this.ghostPossession) {
                    this.ghostView = null;
                }

                this.ghostPossession = null;
            }

            if (this.ghostView == null) {
                setView(player);
            }
        }

        if (this.ghostProjection != null) {
            if (!list.contains(this.ghostProjection.playerBody)) {
                if (this.ghostView == this.ghostProjection && !this.ghostProjectionLock) {
                    this.ghostProjection.movementInput = new MovementInput();
                    player.movementInput = this.playerMovementInput;

                    if (this.optProjectionSpoof) {
                        setView(player);
                        syncCurrentItem();
                    }
                }

                if (this.ghostView == this.ghostProjection) {
                    this.ghostView = null;
                }

                this.ghostProjection = null;
            }

            if (this.ghostView == null) {
                setView(player);
            }
        }

        if (this.ghostView != null) {
            String message = "View: \u00a7b" + ZWrapper.getName(this.ghostView) + "\u00a7f";
            if (getView() == this.ghostProjection) {
                if (getFlying(this.ghostProjection)) {
                    message += " flying";
                }
                if (getNoclip(this.ghostProjection)) {
                    message += " noclip";
                }
            }

            setMessage("view", message);
        } else {
            setMessage("view", null);
        }

        if (!isInMenu()) {
            if (wasKeyPressedThisTick(this.keyPossession)) {
                if (this.ghostPossession != null) {
                    this.ghostPossession = null;
                    this.ghostView = null;
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
                    x2 = x1 + (100f * -Math.sin(yaw) * Math.abs(Math.cos(pitch)));
                    y2 = y1 + (100f * -Math.sin(pitch));
                    z2 = z1 + (100f * Math.cos(yaw) * Math.abs(Math.cos(pitch)));
                    EntityLivingBase best = null;
                    double bestDS = 1000000000f;

                    for (Object obj : list) {
                        if (!(obj instanceof EntityLivingBase) || obj == eye) {
                            continue;
                        }

                        EntityLivingBase ent = (EntityLivingBase) obj;
                        if (!(ent instanceof EntityPlayer)) {
                            // Cannot view from other mobs for now
                            continue;
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
                        if (getView() == this.ghostProjection && !this.ghostProjectionLock) {
                            this.ghostProjection.movementInput = new MovementInput();
                            player.movementInput = this.playerMovementInput;

                            if (this.optProjectionSpoof) {
                                setView(player);
                                syncCurrentItem();
                            }
                        }

                        setView(best);
                        if (best == player) {
                            best = null;
                        }

                        this.ghostView = this.ghostPossession = best;
                    }
                }
            }

            if (wasKeyPressedThisTick(this.keyProjection)) {
                if (this.ghostProjection == null) {
                    this.ghostProjection = new DummyPlayer(player);
                    this.ghostProjection.placeAt(getView());

                    if (getView() == player) {
                        setFlying(this.ghostProjection, getFlying(player));
                        setNoclip(this.ghostProjection, getNoclip(player));
                    }
                }
                if (this.ghostView == this.ghostProjection) {
                    this.ghostView = null;
                    if (!this.ghostProjectionLock) {
                        player.movementInput = this.playerMovementInput;
                        this.ghostProjection.movementInput = new MovementInput();

                        if (this.optProjectionSpoof) {
                            setView(player);
                            syncCurrentItem();
                        }
                    }
                } else {
                    this.ghostView = this.ghostProjection;
                    if (!this.ghostProjectionLock) {
                        player.movementInput = new MovementInput();
                        this.ghostProjection.movementInput = this.playerMovementInput;
                    }
                }

                if (this.ghostView != null) {
                    setView(this.ghostView);
                } else {
                    setView(player);
                }
            }

            if (wasKeyPressedThisTick(this.keyRelocate)) {
                //if (ghostProjection == null) {
                {
                    boolean was = getView() == this.ghostProjection;
                    this.ghostProjection = new DummyPlayer(player);
                    if (was) {
                        setView(this.ghostView = this.ghostProjection);
                    }

                    if (!was || this.ghostProjectionLock) {
                        this.ghostProjection.movementInput = new MovementInput();
                    } else {
                        this.ghostProjection.movementInput = this.playerMovementInput;
                    }

                    if (!was) {
                        setFlying(this.ghostProjection, getFlying((EntityPlayer) getView()));
                    } else if (this.ghostProjectionLock) {
                        setFlying(this.ghostProjection, getFlying(player));
                    }
                }

                this.ghostProjection.placeAt(this.ghostPossession != null ? this.ghostPossession : player);
            }
        }
    }

    private void beforeReset(boolean optReset) {
        if (optReset && this.ghostView != null) {
            setView(this.ghostPlayer);
        }
    }

    private void afterReset(boolean optReset) {
        if (optReset && this.ghostView != null) {
            setView(this.ghostView);
        }
    }

    private void onClientUpdate() {
        if (this.ghostProjection != null && getView() == this.ghostProjection) {
            this.ghostProjection.onUpdate();
        }
    }

    private boolean shouldUpdatePlayerActionState() {
        return getView() == getPlayer() || getView() == this.ghostView;
    }

    private boolean isControllingPlayer() {
        return !this.isControllingProjection();
    }

    private boolean isControllingProjection() {
        return getView() == this.ghostProjection && !this.ghostProjectionLock;
    }

    private boolean isControllingView() {
        return getView() == getPlayer() || this.isControllingProjection();
    }

    @Nullable
    private EntityPlayer getControlledEntity() {
        return this.isControllingProjection() ? this.ghostProjection : getPlayer();
    }

    private boolean allowSwing() {
        return this.isControllingPlayer() || this.optProjectionSwing;
    }

    private boolean allowItemSync() {
        return this.isControllingPlayer() || !this.optProjectionSpoof;
    }

    @Nonnull
    private Orientation onSetAngles(@Nonnull Orientation rot) {
        if (getPlayer() == null || this.isControllingPlayer()) {
            return rot;
        }

        assert this.ghostProjection != null;

        this.ghostProjection.setAngles(rot.yaw, rot.pitch);
        return new Orientation(0, 0);
    }

    private void beforeBlockDig() {
        if (this.isControllingProjection() && this.optProjectionSpoof) {
            if (!this.ghostUnspoof) {
                getPlayerController().switchToRealItem();
            }
            this.ghostUnspoof = true;
        }
    }

    private void afterBlockDig() {
        if (this.isControllingProjection() && this.optProjectionSpoof) {
            if (this.ghostUnspoof) {
                getPlayerController().switchToIdleItem();
            }
            this.ghostUnspoof = false;
        }
    }

    private void beforeBlockPlace() {
        if (this.isControllingProjection() && this.optProjectionSpoof) {
            if (!this.ghostUnspoof) {
                getPlayerController().switchToRealItem();
            }
            this.ghostUnspoof = true;
        }
        if (this.isControllingProjection() && this.optProjectionPlace) {
            assert this.ghostPlayer != null && this.ghostProjection != null;

            this.ghostYaw = getYaw(this.ghostPlayer);
            this.ghostPitch = getPitch(this.ghostPlayer);
            setYaw(this.ghostPlayer, getYaw(this.ghostProjection));
            setPitch(this.ghostPlayer, getPitch(this.ghostProjection));
            sendMotionUpdates(this.ghostPlayer);
        }
    }
}
