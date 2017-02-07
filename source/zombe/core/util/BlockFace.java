package zombe.core.util;


import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import zombe.core.ZWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public class BlockFace {

    public final int x;
    public final int y;
    public final int z;
    public final int side;

    public BlockFace(@Nonnull BlockPos pos) {
        this(getX(pos), getY(pos), getZ(pos), -1);
    }

    public BlockFace(@Nonnull BlockPos pos, EnumFacing face) {
        this(getX(pos), getY(pos), getZ(pos), ZWrapper.getFacing(face));
    }

    public BlockFace(int x, int y, int z) {
        this(x, y, z, -1);
    }

    public BlockFace(int x, int y, int z, int side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
    }

    @Nonnull
    public BlockPos getPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    @Nullable
    public EnumFacing getFacing() {
        if (this.side == -1) {
            throw new RuntimeException();
        }

        return ZWrapper.getFacing(this.side);
    }
}
