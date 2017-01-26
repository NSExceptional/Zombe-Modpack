package zombe.core.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zombe.core.ZWrapper;

import static zombe.core.ZWrapper.*;

public class BlockFace {

    public final int x;
    public final int y;
    public final int z;
    public final int side;

    public BlockFace(BlockPos pos) {
        this(getX(pos),getY(pos),getZ(pos),-1);
    }

    public BlockFace(BlockPos pos, EnumFacing face) {
        this(getX(pos),getY(pos),getZ(pos),ZWrapper.getFacing(face));
    }

    public BlockFace(int x, int y, int z) {
        this(x,y,z,-1);
    }

    public BlockFace(int x, int y, int z, int side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
    }

    public BlockPos getPos() {
        return new BlockPos(x,y,z);
    }

    public EnumFacing getFacing() {
        return ZWrapper.getFacing(side);
    }

}
