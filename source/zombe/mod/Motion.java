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

    private static boolean optFixMovedWrongly, optFixMovedTooQuickly;

    @Nullable private static DummyPlayer serverDummy = null, clientDummy = null;
    @Nullable private static Vec3d sentPosition = null;
    @Nullable private static Vec3d serverPosition = null;
    @Nullable private static Vec3d serverNextPosition = null;
    @Nullable private static Vec3d serverMotion = null;
    @Nullable private static Vec3d serverNextMotion = null;
    @Nullable private static Vec3d clientPosition = null;
    @Nullable private static Vec3d clientMotion = null;
    private static int ticksForForceSync;
    private static boolean anticipated;

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

    private static void checkDummy(@Nonnull EntityPlayer player) {
        if (serverDummy == null || serverDummy.playerBody != player) {
            clientDummy = new DummyPlayer(player);
            serverDummy = new DummyPlayer(player);
            serverMotion = clientMotion = getMotion(player);
            serverPosition = sentPosition = getPosition(player);
        }
    }

    private static void onClientUpdate(@Nonnull EntityPlayer player) {
        checkDummy(player);
        // if position was changed between two updates,
        if (clientPosition != null && (getX(clientPosition) != getPrevX(player) || getY(clientPosition) != getPrevY(player) || getZ(clientPosition) != getPrevZ(player))) {
            // then emulate the handling of S08PacketPlayerPosLook
            serverMotion = clientMotion = getMotion(player);
            serverPosition = getPosition(player);
            emulateHandleMotion(player, getPosition(player), false);
        }
        clientPosition = getPosition(player);
        // if motion was changed between two updates, use it
        Vec3d gain = getMotion(player).subtract(clientMotion);
        //serverMotion = serverMotion.add(gain);
        if (gain.dotProduct(gain) > 0) {
            serverMotion = getMotion(player);
        }
        // anticipated new position and motion
        setPosition(serverDummy, serverPosition);
        setMotion(serverDummy, serverMotion);
        serverDummy.onUpdate();
        serverNextPosition = getPosition(serverDummy);
        serverNextMotion = getMotion(serverDummy);
        anticipated = true;
    }

    private static void beforePlayerMove(Vec3d move) {
        EntityPlayer player = getPlayer();
        if (isSleeping(player) || player.isRiding()) {
            return;
        }
        move = getMotion(player);
        if (optFixMovedTooQuickly && isMultiplayer()) {
            int count = 0;
            while (isMoveTooQuick(clientPosition.add(move))) {
                double mx = getX(move), my = getY(move), mz = getZ(move);
                ++count;
                if (count < 10) {
                    // no idea how to do it yet
                    // placeholder solution
                    mx *= 0.8;
                    my *= 0.8;
                    mz *= 0.8;
                } else if (count == 10) {
                    // smallest movement in motion's direction
                    mx = getX(serverNextPosition) - getX(player);
                    my = getY(serverNextPosition) - getY(player);
                    mz = getZ(serverNextPosition) - getZ(player);
                    double l = Math.sqrt(mx * mx + my * my + mz * mz);
                    double f = 1.01 * (1 - 10 / l);
                    mx *= f;
                    my *= f;
                    mz *= f;
                } else if (count == 11) {
                    // movement = motion
                    log("in mod Motion: FixMovedTooQuickly looped too much");
                    mx = getX(serverNextPosition) - getX(player);
                    my = getY(serverNextPosition) - getY(player);
                    mz = getZ(serverNextPosition) - getZ(player);
                } else {
                    // should never happen
                    log("in mod Motion: FixMovedTooQuickly looped way too much");
                    break;
                }
                move = new Vec3d(mx, my, mz);
            }
        }
        if (optFixMovedWrongly && !isCreative(player) && !getNoclip(player)) {
            boolean wrong;
            do {
                Vec3d noclipDestination = clientPosition.add(move);
                setPosition(clientDummy, clientPosition);
                double mx = getX(move), my = getY(move), mz = getZ(move);
                clientDummy.moveEntity(MoverType.PLAYER, mx, my, mz);
                Vec3d actualDestination = getPosition(clientDummy);
                Vec3d diff = actualDestination.subtract(serverPosition);
                mx = getX(diff);
                my = getY(diff);
                mz = getZ(diff);
                setPosition(clientDummy, serverPosition);
                clientDummy.moveEntity(MoverType.SELF, mx, my, mz);
                Vec3d serverDestination = getPosition(clientDummy);
                if (wrong = wasMoveWrong(actualDestination, serverDestination)) {
                    move = serverDestination.subtract(clientPosition);
                }
            } while (wrong);
        }
        setMotion(player, move);
    }

    private static void afterPlayerMove(Vec3d move) {

    }

    private static void beforeSendMotion(EntityPlayer player) {

    }

    private static void afterSendMotion(@Nonnull EntityPlayer player) {
        clientMotion = getMotion(player);
        emulateSendMotion(player);
    }

    private static void emulateSendMotion(@Nonnull EntityPlayer player) {
        // emulate client player's sendMotion
        Vec3d newPosition = null;
        if (player.isRiding()) {
            // needs improvement
            newPosition = getPosition(player);
            emulateHandleMotion(player, newPosition, getOnGround(player));
        } else if (getView() == player) {
            double dx = getX(sentPosition) - getX(player);
            double dy = getY(sentPosition) - getY(player);
            double dz = getZ(sentPosition) - getZ(player);
            boolean sync = (dx * dx + dy * dy + dz * dz > 9.0E-4D || ticksForForceSync >= 20);
            ++ticksForForceSync;
            if (sync) {
                newPosition = sentPosition = getPosition(player);
                ticksForForceSync = 0;
            }
            emulateHandleMotion(player, newPosition, getOnGround(player));
        }
    }

    private static void emulateHandleMotion(
            @Nonnull EntityPlayer player, @Nullable Vec3d newPosition, boolean packetOnGround) {
        // emulate server handling of C03PacketPlayer
        Vec3d oldPosition = serverPosition;
        if (newPosition == null) {
            newPosition = oldPosition;
        }
        if (player.isRiding()) {
            // needs improvement
            serverPosition = serverNextPosition = newPosition;
            serverMotion = serverNextMotion;
            return;
        }
        if (isSleeping(player)) {
            // needs improvement
            serverPosition = serverNextPosition = oldPosition;
            serverMotion = serverNextMotion;
            return;
        }
        // server update part
        if (!anticipated) {
            setPosition(serverDummy, serverPosition);
            setMotion(serverDummy, serverMotion);
            serverDummy.onUpdate();
            serverNextPosition = getPosition(serverDummy);
            serverNextMotion = getMotion(serverDummy);
        }
        serverPosition = serverNextPosition;
        serverMotion = serverNextMotion;
        anticipated = false;
        // moved too quickly check
        if (isMultiplayer() && isMoveTooQuick(newPosition)) {
            serverNextPosition = serverPosition = oldPosition;
            // note: motion is kept as is and a position packet is sent
            return;
        }
        // server movement attempt
        setPosition(serverDummy, oldPosition);
        double spacing = 0.0625;
        boolean wasFree = getWorld(player).getCollisionBoxes(player, getAABB(serverDummy).contract(spacing)).isEmpty();
        if (getOnGround(serverDummy) && !packetOnGround && getX(player) > getX(oldPosition)) {
            serverDummy.jump();
        }
        Vec3d move = newPosition.subtract(oldPosition);
        serverDummy.moveEntity(MoverType.SELF, getX(move), getY(move), getZ(move));
        setOnGround(serverDummy, packetOnGround);
        // moved wrongly check
        boolean moveWrong = false;
        if (!isSleeping(player) && !isCreative(player) && wasMoveWrong(serverDummy, newPosition)) {
            moveWrong = true;
        }
        setPosition(serverDummy, newPosition);
        if (!getNoclip(player)) {
            boolean isFree = getWorld(player).getCollisionBoxes(player, getAABB(serverDummy).contract(spacing)).isEmpty();
            if (wasFree && (moveWrong || !isFree) && !isSleeping(player)) {
                serverNextPosition = serverPosition = oldPosition;
                // note: motion is kept as is and a position packet is sent
                return;
            }
        }
        setOnGround(serverDummy, packetOnGround);
        // serverUpdateMountedMovingPlayer(serverDummy)
        // serverDummy.handleFalling(getY(serverDummy) - getY(oldPosition), packetOnGround);
        serverNextPosition = serverPosition = getPosition(serverDummy);
        serverNextMotion = serverMotion = getMotion(serverDummy);
    }

    private static boolean isMoveTooQuick(@Nonnull Vec3d move) {
        double nx = Math.abs(getX(move) - getX(serverNextPosition));
        double ny = Math.abs(getY(move) - getY(serverNextPosition));
        double nz = Math.abs(getZ(move) - getZ(serverNextPosition));
        double dx = Math.min(nx, Math.abs(getX(serverNextMotion)));
        double dy = Math.min(ny, Math.abs(getY(serverNextMotion)));
        double dz = Math.min(nz, Math.abs(getZ(serverNextMotion)));
        double dist = dx * dx + dy * dy + dz * dz;
        return dist >= 100;
    }

    private static boolean wasMoveWrong(@Nonnull EntityPlayer what, @Nonnull Vec3d move) {
        return wasMoveWrong(getPosition(what), move);
    }

    private static boolean wasMoveWrong(@Nonnull Vec3d pos1, @Nonnull Vec3d pos2) {
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
        if (name.equals("getServerMotion")) {
            return serverMotion;
        }
        if (name.equals("getServerNextMotion")) {
            return serverNextMotion;
        }
        if (name.equals("getServerPosition")) {
            return serverPosition;
        }
        if (name.equals("getServerNextPosition")) {
            return serverNextPosition;
        }
        if (name.equals("onClientUpdate")) {
            onClientUpdate((EntityPlayer) arg);
        }
        if (name.equals("beforePlayerMove")) {
            beforePlayerMove((Vec3d) arg);
        }
        if (name.equals("afterPlayerMove")) {
            afterPlayerMove((Vec3d) arg);
        }
        if (name.equals("beforeSendMotion")) {
            beforeSendMotion((EntityPlayer) arg);
        }
        if (name.equals("afterSendMotion")) {
            afterSendMotion((EntityPlayer) arg);
        }
        return arg;
    }

    @Override
    protected void init() {
        ticksForForceSync = 0;
        anticipated = false;
        clientPosition = serverPosition = serverNextPosition = sentPosition = clientMotion = serverMotion = serverNextMotion = null;
    }

    @Override
    protected void quit() {
        serverDummy = clientDummy = null;
    }

    @Override
    protected void updateConfig() {
        optFixMovedWrongly = getOptionBool("optMotionFixMovedWrongly");
        optFixMovedTooQuickly = getOptionBool("optMotionFixMovedTooQuickly");
    }

    @Override
    protected void onWorldChange() {
        this.quit();
        this.init();
    }

}
