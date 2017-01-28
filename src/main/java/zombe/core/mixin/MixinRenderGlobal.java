package zombe.core.mixin;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombe.core.ZHandle;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Inject(method = "setupTerrain(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V",
            at = @At("HEAD"))
    private void onSetupTerrain(Entity entity, double ticks, ICamera camera, int frameCount, boolean spectator, CallbackInfo ci) {
        ZHandle.handle("onSortAndRender");
    }
}
