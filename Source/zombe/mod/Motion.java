package zombe.mod;

import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;

import static zombe.core.ZWrapper.*;
import zombe.core.*;
import zombe.core.content.DummyPlayer;
import java.lang.*;

public final class Motion extends ZMod {

    private static boolean optFixMovedWrongly, optFixMovedTooQuickly;

    private static DummyPlayer serverDummy = null, clientDummy = null;
    private static Vec3 sentPosition = null;
    private static Vec3 serverPosition = null;
    private static Vec3 serverNextPosition = null;
    private static Vec3 serverMotion = null;
    private static Vec3 serverNextMotion = null;
    private static Vec3 clientPosition = null;
    private static Vec3 clientMotion = null;
    private static int ticksForForceSync;
    private static boolean anticipated;

    public Motion() {
        super("motion", "1.8", "9.0.2");

        registerHandler("getServerMotion");
        registerHandler("getServerNextMotion");
        registerHandler("getServerPosition");
        registerHandler("getServerNextPosition");

        registerListener("onClientUpdate");
        registerListener("beforePlayerMove");
        //registerListener("afterPlayerMove");
        //registerHandler("beforeSendMotion");
        registerHandler("afterSendMotion");

        addOption("experimental server-side movement prediction");
        addOption("optMotionFixMovedWrongly", "Avoid 'player moved wrongly'", true);
        addOption("optMotionFixMovedTooQuickly", "Avoid 'player moved too quickly'", true);
    }

    @Override
    protected void updateConfig() {
        optFixMovedWrongly    = getOptionBool("optMotionFixMovedWrongly");
        optFixMovedTooQuickly = getOptionBool("optMotionFixMovedTooQuickly");
    }

    @Override
    protected void init() {
        ticksForForceSync = 0;
        anticipated = false;
        clientPosition = serverPosition = serverNextPosition = sentPosition
            = clientMotion = serverMotion = serverNextMotion = null;
    }

    @Override
    protected void quit() {
        serverDummy = clientDummy = null;
    }

    @Override
    protected void onWorldChange() {
        quit();
        init();
    }

    @Override
    protected Object handle(String name, Object arg) {
        if (name == "getServerMotion")
            return serverMotion;
        if (name == "getServerNextMotion")
            return serverNextMotion;
        if (name == "getServerPosition")
            return serverPosition;
        if (name == "getServerNextPosition")
            return serverNextPosition;
        if (name == "onClientUpdate")
            onClientUpdate((EntityPlayer) arg);
        if (name == "beforePlayerMove")
            beforePlayerMove((Vec3) arg);
        if (name == "afterPlayerMove")
            afterPlayerMove((Vec3) arg);
        if (name == "beforeSendMotion")
            beforeSendMotion((EntityPlayer) arg);
        if (name == "afterSendMotion")
            afterSendMotion((EntityPlayer) arg);
        return arg;
    }

    private static void checkDummy(EntityPlayer player) {
        if (serverDummy == null || serverDummy.playerBody != player) {
            clientDummy = new DummyPlayer(player);
            serverDummy = new DummyPlayer(player);
            serverMotion   = clientMotion = getMotion(player);
            serverPosition = sentPosition = getPosition(player);
        }
    }

    private static void onClientUpdate(EntityPlayer player) {
        checkDummy(player);
        // if position was changed between two updates,
        if (clientPosition != null
            && (getX(clientPosition) != getPrevX(player)
             || getY(clientPosition) != getPrevY(player)
             || getZ(clientPosition) != getPrevZ(player))) {
            // then emulate the handling of S08PacketPlayerPosLook
            serverMotion   = clientMotion = getMotion(player);
            serverPosition = getPosition(player);
            emulateHandleMotion(player, getPosition(player), false);
        }
        clientPosition = getPosition(player);
        // if motion was changed between two updates, use it
        Vec3 gain = getMotion(player).subtract(clientMotion);
        //serverMotion = serverMotion.add(gain);
        if (gain.dotProduct(gain) > 0) serverMotion = getMotion(player);
        // anticipated new position and motion
        setPosition(serverDummy, serverPosition);
        setMotion(serverDummy,   serverMotion);
        serverDummy.onUpdate();
        serverNextPosition = getPosition(serverDummy);
        serverNextMotion   = getMotion(serverDummy);
        anticipated = true;
    }

    private static void beforePlayerMove(Vec3 move) {
        EntityPlayer player = getPlayer();
        if (isSleeping(player) || player.isRiding()) return;
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
                    mx = getX(serverNextPosition) -getX(player);
                    my = getY(serverNextPosition) -getY(player);
                    mz = getZ(serverNextPosition) -getZ(player);
                    double l = Math.sqrt(mx*mx + my*my + mz*mz);
                    double f = 1.01 * (1 - 10/l);
                    mx *= f;
                    my *= f;
                    mz *= f;
                } else if (count == 11) {
                    // movement = motion
                    log("in mod Motion: FixMovedTooQuickly looped too much");
                    mx = getX(serverNextPosition) -getX(player);
                    my = getY(serverNextPosition) -getY(player);
                    mz = getZ(serverNextPosition) -getZ(player);
                } else {
                    // should never happen
                    log("in mod Motion: FixMovedTooQuickly looped way too much");
                    break;
                }
                move = new Vec3(mx,my,mz);
            }
        }
        if (optFixMovedWrongly && !isCreative(player) && !getNoclip(player)) {
            boolean wrong;
            do {
                Vec3 noclipDestination = clientPosition.add(move);
                setPosition(clientDummy, clientPosition);
                double mx = getX(move), my = getY(move), mz = getZ(move);
                clientDummy.moveEntity(mx,my,mz);
                Vec3 actualDestination = getPosition(clientDummy);
                Vec3 diff = actualDestination.subtract(serverPosition);
                mx = getX(diff); my = getY(diff); mz = getZ(diff);
                setPosition(clientDummy, serverPosition);
                clientDummy.moveEntity(mx,my,mz);
                Vec3 serverDestination = getPosition(clientDummy);
                if (wrong = wasMoveWrong(actualDestination, serverDestination)) {
                    move = serverDestination.subtract(clientPosition);
                }
            } while (wrong);
        }
        setMotion(player, move);
    }

    private static void afterPlayerMove(Vec3 move) {
        
    }

    private static void beforeSendMotion(EntityPlayer player) {

    }

    private static void afterSendMotion(EntityPlayer player) {
        clientMotion = getMotion(player);
        emulateSendMotion(player);
    }

    private static void emulateSendMotion(EntityPlayer player) {
        // emulate client player's sendMotion
        Vec3 newPosition = null;
        if (player.isRiding()) {
            // needs improvement
            newPosition = getPosition(player);
            emulateHandleMotion(player, newPosition, getOnGround(player));
        } else if (getView() == player) {
            double dx = getX(sentPosition) - getX(player);
            double dy = getY(sentPosition) - getY(player);
            double dz = getZ(sentPosition) - getZ(player);
            boolean sync = (dx*dx + dy*dy + dz*dz > 9.0E-4D 
                || ticksForForceSync >= 20);
            ++ticksForForceSync;
            if (sync) {
                newPosition = sentPosition = getPosition(player);
                ticksForForceSync = 0;
            }
            emulateHandleMotion(player, newPosition, getOnGround(player));
        }
    }

    private static void emulateHandleMotion(EntityPlayer player, Vec3 newPosition, boolean packetOnGround) {
        // emulate server handling of C03PacketPlayer
        Vec3 oldPosition = serverPosition;
        if (newPosition == null) newPosition = oldPosition;
        if (player.isRiding()) {
            // needs improvement
            serverPosition = serverNextPosition = newPosition;
            serverMotion   = serverNextMotion;
            return;
        }
        if (isSleeping(player)) {
            // needs improvement
            serverPosition = serverNextPosition = oldPosition;
            serverMotion   = serverNextMotion;
            return;
        }
        // server update part
        if (!anticipated) {
            setPosition(serverDummy, serverPosition);
            setMotion(serverDummy,   serverMotion);
            serverDummy.onUpdate();
            serverNextPosition = getPosition(serverDummy);
            serverNextMotion   = getMotion(serverDummy);
        }
        serverPosition = serverNextPosition;
        serverMotion   = serverNextMotion;
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
        boolean wasFree = getWorld(player).getCollidingBoundingBoxes(player, getAABB(serverDummy).contract(spacing, spacing, spacing)).isEmpty();
        if (getOnGround(serverDummy) && !packetOnGround
         && getX(player) > getX(oldPosition)) {
            serverDummy.jump();
        }
        Vec3 move = newPosition.subtract(oldPosition);
        serverDummy.moveEntity(getX(move), getY(move), getZ(move));
        setOnGround(serverDummy, packetOnGround);
        // moved wrongly check
        boolean moveWrong = false;
        if (!isSleeping(player) && !isCreative(player)
         && wasMoveWrong(serverDummy, newPosition)) {
            moveWrong = true;
        }
        setPosition(serverDummy, newPosition);
        if (!getNoclip(player)) {
            boolean isFree = getWorld(player).getCollidingBoundingBoxes(player, getAABB(serverDummy).contract(spacing, spacing, spacing)).isEmpty();
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
        serverNextMotion   = serverMotion   = getMotion(serverDummy);
    }

    private static boolean isMoveTooQuick(Vec3 move) {
        double nx = Math.abs(getX(move) - getX(serverNextPosition));
        double ny = Math.abs(getY(move) - getY(serverNextPosition));
        double nz = Math.abs(getZ(move) - getZ(serverNextPosition));
        double dx = Math.min(nx, Math.abs(getX(serverNextMotion)));
        double dy = Math.min(ny, Math.abs(getY(serverNextMotion)));
        double dz = Math.min(nz, Math.abs(getZ(serverNextMotion)));
        double dist = dx*dx + dy*dy + dz*dz;
        return dist >= 100;
    }

    private static boolean wasMoveWrong(EntityPlayer what, Vec3 move) {
        return wasMoveWrong(getPosition(what), move);
    }

    private static boolean wasMoveWrong(Vec3 pos1, Vec3 pos2) {
        double dx = getX(pos1) - getX(pos2);
        double dy = getY(pos1) - getY(pos2);
        double dz = getZ(pos1) - getZ(pos2);
        if (-0.5 < dy && dy < 0.5) dy = 0;
        double dist = dx*dx + dy*dy + dz*dz;
        return dist >= 0.0625;
    }

}
