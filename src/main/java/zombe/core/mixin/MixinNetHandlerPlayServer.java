package zombe.core.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombe.core.ZHandle;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP player;

    @Inject(method = "update()V", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        ZHandle.onNetworkTick(this.player);
    }

    @ModifyConstant(method = "processPlayerDigging", constant = @Constant(doubleValue = 36D))
    private double getPlayerReachDigSq(double value) {
        return ZHandle.handle("getPlayerReachDigSq", value);
    }

    @ModifyConstant(method = "processUseEntity", constant = @Constant(doubleValue = 36D))
    private double getPlayerReachUseSq(double value) {
        return ZHandle.handle("getPlayerReachUseSq", value);
    }
}
