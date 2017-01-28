package zombe.core.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zombe.core.ZHandle;
import zombe.core.ZWrapper;

@SuppressWarnings("EntityConstructor")
@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements IContainerListener{

    public MixinEntityPlayerMP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "onUpdateEntity()V", at = @At("HEAD"))
    private void onUpdateEntity(CallbackInfo ci) {
        //-ZMod-----------------------------------------------------------
        ZHandle.handle("onServerUpdate",this);
    }

    @Inject(method = "onDeath(Lnet/minecraft/util/DamageSource;)V", at = @At("HEAD"))
    private void onDeath(DamageSource damage, CallbackInfo ci) {
        //-ZMod-Death-----------------------------------------------------------
        ZHandle.handle("onPlayerDeath",this);
    }

    @Inject(method = "isSpectator()Z", at = @At("HEAD"))
    private void isSpectator(CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Fly-noclip----------------------------------------------------
        if (ZHandle.handle("isNoclip", this, false)) ZWrapper.setNoclip(this, true);
    }
}
