package zombe.core.mixin;


import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombe.core.ZHandle;
import zombe.core.ZWrapper;
import zombe.core.util.Orientation;

import javax.annotation.Nullable;

@SuppressWarnings("EntityConstructor")
@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    private static final String ACP = "Lnet/minecraft/client/entity/AbstractClientPlayer;";

    @Shadow
    protected Minecraft mc;

    @SuppressWarnings("unused")
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "onUpdate()V",
            at = @At(value = "INVOKE",
                    target = ACP + "onUpdate()V",
                    shift = At.Shift.BEFORE))
    private void onClientUpdate(CallbackInfo ci) {
        ZHandle.handle("onClientUpdate", this);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void beforeSendMotion(CallbackInfo ci) {
        ZHandle.handle("beforeSendMotion", this);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void afterSendMotion(CallbackInfo ci) {
        ZHandle.handle("afterSendMotion", this);
    }

    @Inject(method = "swingArm(Lnet/minecraft/util/EnumHand;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void allowSwing(EnumHand hand, CallbackInfo ci) {
        if (!ZHandle.handle("allowSwing", true)) ci.cancel();
    }

    @Redirect(method = "onLivingUpdate()V",
            at = @At(value = "FIELD",
                    ordinal = 0,
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onGround:Z"))
    private boolean allowVanillaSprint(EntityPlayerSP entity) {
        return ZHandle.handle("allowVanillaSprint", true) && this.onGround;
    }

    @Redirect(method = "onLivingUpdate()V",
            at = @At(value = "FIELD",
                    ordinal = 1,
                    target = "Lnet/minecraft/entity/player/PlayerCapabilities;allowFlying:Z"))
    private boolean allowVanillaFly(PlayerCapabilities capabilities) {
        return ZHandle.handle("allowVanillaFly", true) && capabilities.allowFlying;
    }

    @Redirect(method = "onLivingUpdate()V",
            at = @At(value = "FIELD",
                    ordinal = 4,
                    target = "Lnet/minecraft/entity/player/PlayerCapabilities;isFlying:Z"))
    private boolean isFlying(PlayerCapabilities capabilities) {
        return ZHandle.handle("isFlying", this, false) && capabilities.isFlying;
    }

    @Inject(method = "onLivingUpdate()V",
            at = @At(value = "INVOKE",
                    target = ACP + "onLivingUpdate()V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void afterLivingUpdate(CallbackInfo ci) {
        ci.cancel();
        if (ZHandle.handle("isPlayerOnGround", this.onGround)
                && this.capabilities.isFlying
                && !this.mc.playerController.isSpectatorMode()
                && !ZHandle.handle("isFlying", this, false)) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    private Vec3d motion;

    @Inject(method = "move(Lnet/minecraft/entity/MoverType;DDD)V", at = @At("HEAD"))
    private void beforePlayerMove(MoverType type, double mx, double my, double mz, CallbackInfo ci) {
        ZHandle.handle("beforePlayerMove", new Vec3d(mx, my, mz));
        motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
    }

    @Inject(method = "move(Lnet/minecraft/entity/MoverType;DDD)V", at = @At("RETURN"))
    private void afterPlayerMove(MoverType type, double mx, double my, double mz, CallbackInfo ci) {
        ZHandle.handle("afterPlayerMove", motion);
        motion = null;
    }

    @Override
    protected void setRotation(float yaw, float pitch) {
        Orientation rot = (Orientation) ZHandle.handle("onSetAngles", new Orientation(yaw, pitch));
        super.setRotation(rot.yaw, rot.pitch);
    }

    @Nullable
    @Override
    public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
        RayTraceResult trace = super.rayTrace(blockReachDistance, partialTicks);
        return (RayTraceResult) ZHandle.handle("onPlayerRayTrace", trace);
    }

    @Override
    public void jump() {
        super.jump();
        ZHandle.handle("onPlayerJump", this);
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return !ZHandle.handle("ignorePlayerInsideOpaqueBlock", false) && super.isEntityInsideOpaqueBlock();
    }

    @Override
    public boolean isSpectator() {
        if (ZHandle.handle("isNoclip", this, false))
            ZWrapper.setNoclip(this, true);
        return super.isSpectator();
    }
}
