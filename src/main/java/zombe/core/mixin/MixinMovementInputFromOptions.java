package zombe.core.mixin;

import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zombe.core.ZHandle;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions {

    @Redirect(method = "updatePlayerMoveState()V",
            at = @At(value = "FIELD",
            target = "Lnet/minecraft/util/MovementInputFromOptions;sneak:Z",
            ordinal = 1))
    private boolean isFlying(MovementInputFromOptions movement) {
        return !ZHandle.handle("isFlying",false) && movement.sneak;
    }
}
