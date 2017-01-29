package zombe.core.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockFire.class)
public interface IBlockFire {

    @Invoker("getFlammability")
    int getFlammability(Block blockIn);

    @Invoker("getEncouragement")
    int getEncouragement(Block blockIn);
}
