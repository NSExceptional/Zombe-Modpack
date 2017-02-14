package zombe.mod;


import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import zombe.core.ZMod;
import zombe.core.content.DummyPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Motion extends ZMod {

    private boolean optFixMovedWrongly, optFixMovedTooQuickly;
    private int ticksForForceSync;
    private boolean anticipated;
    @Nullable private DummyPlayer serverDummy = null, clientDummy = null;
    @Nullable private Vec3d sentPosition = null;
    @Nullable private Vec3d serverPosition = null;
    @Nullable private Vec3d serverNextPosition = null;
    @Nullable private Vec3d serverMotion = null;
    @Nullable private Vec3d serverNextMotion = null;
    @Nullable private Vec3d clientPosition = null;
    @Nullable private Vec3d clientMotion = null;

    public Motion() {
        super("motion", "1.8", "9.0.2");

        this.registerHandler("getServerMotion");
        this.registerHandler("getServerNextMotion");
        this.registerHandler("getServerPosition");
        this.registerHandler("getServerNextPosition");

        this.registerListener("onClientUpdate");
        this.registerListener("beforePlayerMove");
        //registerListener("afterPlayerMove");
        //registerHandler("beforeSendMotion");
        this.registerHandler("afterSendMotion");

        this.addOption("experimental server-side movement prediction");
        this.addOption("optMotionFixMovedWrongly", "Avoid 'player moved wrongly'", true);
        this.addOption("optMotionFixMovedTooQuickly", "Avoid 'player moved too quickly'", true);
    }

    private void checkDummy(@Nonnull EntityPlayer player) {
        if (this.serverDummy == null || this.serverDummy.playerBody != player) {
            this.clientDummy = new DummyPlayer(player);
            this.serverDummy = new DummyPlayer(player);
            this.serverMotion = this.clientMotion = getMotion(player);
            this.serverPosition = this.sentPosition = getPosition(player);
        }
    }

    private void onClientUpdate(@Nonnull EntityPlayer player) {
        // Call will set this.serverDummy && this.clientMotion if necessary
        this.checkDummy(player);
        assert this.serverDummy != null && this.clientMotion != null;

        // if position was changed between two updates,
        if (this.clientPosition != null && (getX(this.clientPosition) != getPrevX(player)
                                         || getY(this.clientPosition) != getPrevY(player)
                                         || getZ(this.clientPosition) != getPrevZ(player))) {
            // then emulate the handling of S08PacketPlayerPosLook
            this.serverMotion = this.clientMotion = getMotion(player);
            this.serverPosition = getPosition(player);
            this.emulateHandleMotion(player, getPosition(player), false);
        }

        this.clientPosition = getPosition(player);
        // if motion was changed between two updates, use it
        Vec3d gain = getMotion(player).subtract(this.clientMotion);
        //serverMotion = serverMotion.add(gain);
        if (gain.dotProduct(gain) > 0) {
            this.serverMotion = getMotion(player);
        }

        // anticipated new position and motion
        setPosition(this.serverDummy, this.serverPosition);
        setMotion(this.serverDummy, this.serverMotion);
        this.serverDummy.onUpdate();
        this.serverNextPosition = getPosition(this.serverDummy);
        this.serverNextMotion = getMotion(this.serverDummy);
        this.anticipated = true;
    }

    private void beforePlayerMove(Vec3d move) {
        EntityPlayer player = getPlayer();
        if (player == null || isSleeping(player) || player.isRiding()) {
            return;
        }

        move = getMotion(player);
        if (this.optFixMovedTooQuickly && isMultiplayer()) {
            assert this.clientPosition != null;
            int count = 0;

            while (this.isMoveTooQuick(this.clientPosition.add(move))) {

                double mx = getX(move), my = getY(move), mz = getZ(move);
                count++;
                if (count < 10) {
                    // TODO dunno what this was for
                    //
                    // > no idea how to do it yet
                    // > placeholder solution
                    mx *= 0.8;
                    my *= 0.8;
                    mz *= 0.8;
                } else if (count == 10) {
                    assert this.serverNextPosition != null;

                    // smallest movement in motion's direction
                    mx = getX(this.serverNextPosition) - getX(player);
                    my = getY(this.serverNextPosition) - getY(player);
                    mz = getZ(this.serverNextPosition) - getZ(player);
                    double l = Math.sqrt(mx * mx + my * my + mz * mz);
                    double f = 1.01 * (1 - 10 / l);
                    mx *= f;
                    my *= f;
                    mz *= f;
                } else if (count == 11) {
                    assert this.serverNextPosition != null;

                    // movement = motion
                    log("in mod Motion: FixMovedTooQuickly looped too much");
                    mx = getX(this.serverNextPosition) - getX(player);
                    my = getY(this.serverNextPosition) - getY(player);
                    mz = getZ(this.serverNextPosition) - getZ(player);
                } else {
                    // should never happen
                    log("in mod Motion: FixMovedTooQuickly looped way too much");
                    break;
                }
                move = new Vec3d(mx, my, mz);
            }
        }

        if (this.optFixMovedWrongly && !isCreative(player) && !getNoclip(player)) {
            assert this.clientDummy != null && this.serverPosition != null;

            boolean wrong;
            do {
                setPosition(this.clientDummy, this.clientPosition);
                double mx = getX(move), my = getY(move), mz = getZ(move);
                this.clientDummy.moveEntity(MoverType.PLAYER, mx, my, mz);
                Vec3d actualDestination = getPosition(this.clientDummy);
                Vec3d diff = actualDestination.subtract(this.serverPosition);
                mx = getX(diff);
                my = getY(diff);
                mz = getZ(diff);
                setPosition(this.clientDummy, this.serverPosition);
                this.clientDummy.moveEntity(MoverType.SELF, mx, my, mz);
                Vec3d serverDestination = getPosition(this.clientDummy);
                if (wrong = this.wasMoveWrong(actualDestination, serverDestination)) {
                    move = serverDestination.subtract(this.clientPosition);
                }
            } while (wrong);
        }

        setMotion(player, move);
    }

    private void afterPlayerMove(Vec3d move) {

    }

    private void beforeSendMotion(EntityPlayer player) {

    }

    private void afterSendMotion(@Nonnull EntityPlayer player) {
        this.clientMotion = getMotion(player);
        this.emulateSendMotion(player);
    }

    private void emulateSendMotion(@Nonnull EntityPlayer player) {
        // emulate client player's sendMotion
        Vec3d newPosition = null;
        if (player.isRiding()) {
            // needs improvement
            newPosition = getPosition(player);
            this.emulateHandleMotion(player, newPosition, getOnGround(player));
        } else if (getView() == player) {
            assert this.sentPosition != null;

            double dx = getX(this.sentPosition) - getX(player);
            double dy = getY(this.sentPosition) - getY(player);
            double dz = getZ(this.sentPosition) - getZ(player);
            boolean sync = dx * dx + dy * dy + dz * dz > 9.0E-4D || this.ticksForForceSync >= 20;
            this.ticksForForceSync++;
            if (sync) {
                newPosition = this.sentPosition = getPosition(player);
                this.ticksForForceSync = 0;
            }

            this.emulateHandleMotion(player, newPosition, getOnGround(player));
        }
    }

    private void emulateHandleMotion(@Nonnull EntityPlayer player, @Nullable Vec3d newPosition, boolean packetOnGround) {
        assert this.serverPosition != null;

        // emulate server handling of C03PacketPlayer
        Vec3d oldPosition = this.serverPosition;
        if (newPosition == null) {
            newPosition = oldPosition;
        }

        if (player.isRiding()) {
            // needs improvement
            this.serverPosition = this.serverNextPosition = newPosition;
            this.serverMotion = this.serverNextMotion;
            return;
        }
        if (isSleeping(player)) {
            // needs improvement
            this.serverPosition = this.serverNextPosition = oldPosition;
            this.serverMotion = this.serverNextMotion;
            return;
        }

        // server update part
        if (!this.anticipated) {
            assert this.serverDummy != null && this.serverMotion != null;

            setPosition(this.serverDummy, this.serverPosition);
            setMotion(this.serverDummy, this.serverMotion);
            this.serverDummy.onUpdate();
            this.serverNextPosition = getPosition(this.serverDummy);
            this.serverNextMotion = getMotion(this.serverDummy);
        }

        this.serverPosition = this.serverNextPosition;
        this.serverMotion = this.serverNextMotion;
        this.anticipated = false;

        // moved too quickly check
        if (isMultiplayer() && this.isMoveTooQuick(newPosition)) {
            this.serverNextPosition = this.serverPosition = oldPosition;
            // note: motion is kept as is and a position packet is sent
            return;
        }

        // server movement attempt
        setPosition(this.serverDummy, oldPosition);
        double spacing = 0.0625;
        boolean wasFree = getWorld(player).getCollisionBoxes(player, getAABB(this.serverDummy).contract(spacing)).isEmpty();
        if (getOnGround(this.serverDummy) && !packetOnGround && getX(player) > getX(oldPosition)) {
            this.serverDummy.jump();
        }

        Vec3d move = newPosition.subtract(oldPosition);
        this.serverDummy.moveEntity(MoverType.SELF, getX(move), getY(move), getZ(move));
        setOnGround(this.serverDummy, packetOnGround);
        // moved wrongly check
        boolean moveWrong = false;
        if (!isSleeping(player) && !isCreative(player) && this.wasMoveWrong(this.serverDummy, newPosition)) {
            moveWrong = true;
        }

        setPosition(this.serverDummy, newPosition);
        if (!getNoclip(player)) {
            boolean isFree = getWorld(player).getCollisionBoxes(player, getAABB(this.serverDummy).contract(spacing)).isEmpty();
            if (wasFree && (moveWrong || !isFree) && !isSleeping(player)) {
                this.serverNextPosition = this.serverPosition = oldPosition;
                // note: motion is kept as is and a position packet is sent
                return;
            }
        }

        setOnGround(this.serverDummy, packetOnGround);
        // serverUpdateMountedMovingPlayer(serverDummy)
        // serverDummy.handleFalling(getY(serverDummy) - getY(oldPosition), packetOnGround);
        this.serverNextPosition = this.serverPosition = getPosition(this.serverDummy);
        this.serverNextMotion = this.serverMotion = getMotion(this.serverDummy);
    }

    private boolean isMoveTooQuick(@Nonnull Vec3d move) {
        assert this.serverNextPosition != null && this.serverNextMotion != null;

        double nx = Math.abs(getX(move) - getX(this.serverNextPosition));
        double ny = Math.abs(getY(move) - getY(this.serverNextPosition));
        double nz = Math.abs(getZ(move) - getZ(this.serverNextPosition));
        double dx = Math.min(nx, Math.abs(getX(this.serverNextMotion)));
        double dy = Math.min(ny, Math.abs(getY(this.serverNextMotion)));
        double dz = Math.min(nz, Math.abs(getZ(this.serverNextMotion)));
        double dist = dx * dx + dy * dy + dz * dz;
        return dist >= 100;
    }

    private boolean wasMoveWrong(@Nonnull EntityPlayer what, @Nonnull Vec3d move) {
        return this.wasMoveWrong(getPosition(what), move);
    }

    private boolean wasMoveWrong(@Nonnull Vec3d pos1, @Nonnull Vec3d pos2) {
        double dx = getX(pos1) - getX(pos2);
        double dy = getY(pos1) - getY(pos2);
        double dz = getZ(pos1) - getZ(pos2);

        if (-0.5 < dy && dy < 0.5) {
            dy = 0;
        }

        double dist = dx * dx + dy * dy + dz * dz;
        return dist >= 0.0625;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        switch (name) {
            case "getServerMotion":
                return this.serverMotion;
            case "getServerNextMotion":
                return this.serverNextMotion;
            case "getServerPosition":
                return this.serverPosition;
            case "getServerNextPosition":
                return this.serverNextPosition;

            case "onClientUpdate":
                this.onClientUpdate((EntityPlayer) arg);
                break;

            case "beforePlayerMove":
                this.beforePlayerMove((Vec3d) arg);
                break;

            case "afterPlayerMove":
                this.afterPlayerMove((Vec3d) arg);
                break;

            case "beforeSendMotion":
                this.beforeSendMotion((EntityPlayer) arg);
                break;

            case "afterSendMotion":
                this.afterSendMotion((EntityPlayer) arg);
                break;

        }

        return arg;
    }

    @Override
    protected void init() {
        this.ticksForForceSync = 0;
        this.anticipated = false;
        this.clientPosition = null;
        this.serverPosition = null;
        this.serverNextPosition = null;
        this.sentPosition = null;
        this.clientMotion = null;
        this.serverMotion = null;
        this.serverNextMotion = null;
    }

    @Override
    protected void quit() {
        this.serverDummy = this.clientDummy = null;
    }

    @Override
    protected void updateConfig() {
        this.optFixMovedWrongly = getOptionBool("optMotionFixMovedWrongly");
        this.optFixMovedTooQuickly = getOptionBool("optMotionFixMovedTooQuickly");
    }

    @Override
    protected void onWorldChange() {
        this.quit();
        this.init();
    }
}
