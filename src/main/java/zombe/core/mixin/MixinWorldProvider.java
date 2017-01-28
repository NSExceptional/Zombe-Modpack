package zombe.core.mixin;

import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zombe.core.ZHandle;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    private long zSunOffset;

    @Inject(method = "calculateCelestialAngle(JF)F", at = @At("HEAD"))
    private void onCaclulateCelestialAngle(long worldTime, float ticks, CallbackInfoReturnable<Float> ci) {
        //-ZMod---------------------------------------------------------------
        zSunOffset = ZHandle.handle("getSunOffset", 0L);
    }

    @ModifyVariable(method = "calculateCelestialAngle(JF)F", at = @At(value = "HEAD", shift = At.Shift.AFTER))
    private long calculateCelestialAngle(long worldTime) {
        return worldTime + zSunOffset;
    }

    @ModifyVariable(method = "calculateCelestialAngle(JF)F", at = @At(value = "HEAD", shift = At.Shift.AFTER))
    private float calculateCelestialAngle(float partialTicks) {
        if (zSunOffset != 0) {
            partialTicks = 0f;
        }
        return partialTicks;
    }

    @Inject(method = "getCloudHeight()F", at = @At("RETURN"), cancellable = true)
    private void getCloudHeight(CallbackInfoReturnable<Float> s) {
        //-ZMod-Cloud-------------------------------------------------------------
        s.setReturnValue(ZHandle.handle("getCloudHeight", s.getReturnValueF()));
    }
}
