package zombe.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombe.core.ZHandle;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "init()V", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        ZHandle.onMinecraftInit((Minecraft) (Object) this);
    }

    @Inject(method = "runTick()V", at = @At("HEAD"))
    private void onRunTick(CallbackInfo ci) {
        ZHandle.onMinecraftTick();
    }

    @Redirect(method = "runTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;updateRenderer()V"))
    private void onUpdateRenderer(EntityRenderer entityRenderer) {
        ZHandle.handle("beforeUpdateRenderer");
        try {
            entityRenderer.updateRenderer();
        } catch (Exception e) {
            ZHandle.handle("catchUpdateRenderer", e);
        }
        ZHandle.handle("afterUpdateRenderer");
    }

    @Redirect(method = "runTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;getMouseOver(F)V"))
    private void onGetMouseOver(EntityRenderer entityRenderer, float arg) {
        ZHandle.handle("beforeGetMouseOver", arg);
        try {
            entityRenderer.getMouseOver(arg);
        } catch (Exception e) {
            ZHandle.handle("catchGetMouseOver", e);
        }
        ZHandle.handle("afterGetMouseOver", arg);
    }

}
