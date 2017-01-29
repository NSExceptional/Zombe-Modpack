package zombe.core.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombe.core.ZHandle;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    protected abstract void renderRainSnow(float partialTicks);

    @Inject(method = "updateCameraAndRender(FJ)V", at = @At("RETURN"))
    private void onUpdateCameraAndRender(float partialTicks, long time, CallbackInfo ci) {
        ZHandle.onUpdateCameraAndRender(partialTicks);
    }

    @Redirect(method = "renderWorldPass(IFJ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V"))
    private void onRenderRainSnow(EntityRenderer renderer, float f) {
        ZHandle.beginRenderRainSnow(f);
        //noinspection ConstantConditions
        if (ZHandle.forwardRenderRainSnow()) {
            this.renderRainSnow(f);
        }
        ZHandle.endRenderRainSnow(f);
    }

    @Redirect(method = "renderWorld(FJ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;getMouseOver(F)V"))
    private void onGetMouseOver(EntityRenderer renderer, float par1) {
        ZHandle.handle("beforeGetMouseOver", par1);
        try {
            renderer.getMouseOver(par1);
        } catch (Exception e) {
            ZHandle.handle("catchGetMouseOver", e);
        }
        ZHandle.handle("afterGetMouseOver", par1);
    }
}
