package zombe.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zombe.core.ZHandle;
import zombe.core.ducks.IPlayerController;
import zombe.core.util.BlockFace;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements IPlayerController {

    private static final String ENTITY_PLAYER_SP = "Lnet/minecraft/client/entity/EntityPlayerSP;",
            ENTITY_PLAYER = "Lnet/minecraft/entity/player/EntityPlayer;",
            ENTITY = "Lnet/minecraft/entity/Entity;",
            WORLD_CLIENT = "Lnet/minecraft/client/multiplayer/WorldClient;",
            BLOCK_POS = "Lnet/minecraft/util/math/BlockPos;",
            ENUM_FACING = "Lnet/minecraft/util/EnumFacing;",
            VEC3D = "Lnet/minecraft/util/math/Vec3d;",
            ENUM_HAND = "Lnet/minecraft/util/EnumHand;",
            ENUM_ACTION_RESULT = "Lnet/minecraft/util/EnumActionResult;",
            NET_HANDLER_PLAY_CLIENT = "Lnet/minecraft/client/network/NetHandlerPlayClient;",
            PACKET = "Lnet/minecraft/network/Packet;",
            SEND_PACKET = NET_HANDLER_PLAY_CLIENT + "sendPacket(" + PACKET + ")V";

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private NetHandlerPlayClient connection;

    @Shadow
    private int currentPlayerItem;
    @Shadow
    private int blockHitDelay;

    @Shadow
    public abstract void resetBlockRemoving();

    @Shadow
    private boolean isHittingBlock;

    @Override
    @Invoker("syncCurrentPlayItem")
    public abstract void syncCurrentItem();

    @Override
    public void switchToRealItem() {
        int realItem = this.mc.player.inventory.currentItem;
        if (realItem != this.currentPlayerItem) {
            this.connection.sendPacket(new CPacketHeldItemChange(realItem));
        }
    }

    @Override
    public void switchToIdleItem() {
        int realItem = this.mc.player.inventory.currentItem;
        if (realItem != this.currentPlayerItem) {
            this.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
        }
    }

    @ModifyConstant(method = "clickBlock(" + BLOCK_POS + ENUM_FACING + ")Z", constant = @Constant(intValue = 5))
    private int getBlockHitDelay$click(int i) {
        return ZHandle.handle("getBlockHitDelay", i);
    }

    @Inject(method = "clickBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;blockHitDelay:I",
                    shift = At.Shift.AFTER))
    private void onBlockDiggedCreative(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> ci) {
        ZHandle.handle("onBlockDigged", new BlockFace(loc, face));
    }

    @Inject(method = "clickBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;isHittingBlock:Z",
                    ordinal = 1,
                    shift = At.Shift.AFTER))
    private void beforeBlockDig(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Cheat-------------------------------------------------
        ZHandle.handle("beforeBlockDig");
    }

    @Inject(method = "clickBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "INVOKE",
                    target = WORLD_CLIENT + "sendBlockBreakProgress(I" + BLOCK_POS + "I)V",
                    shift = At.Shift.AFTER))
    private void onBlockDiggedSurvival(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Dig-sync----------------------------------------------
        ZHandle.handle("onBlockDigged", new BlockFace(loc, face));
    }

    @Inject(method = "resetBlockRemoving()V",
            at = @At(value = "INVOKE",
                    target = SEND_PACKET,
                    shift = At.Shift.AFTER))
    private void afterBlockDig(CallbackInfo ci) {
        //-ZMod-----------------------------------------------------------
        ZHandle.handle("afterBlockDig");
    }

    @Inject(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At("HEAD"),
            cancellable = true)
    private void checkReachDig(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Dig-check-----------------------------------------------------
        if (!ZHandle.handle("checkReachDig", new BlockFace(posBlock, directionFacing), true)) {
            this.resetBlockRemoving();
            ci.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z", constant = @Constant(intValue = 5))
    private int getBlockHitDelay$damage(int delay) {
        return ZHandle.handle("getBlockHitDelay", delay);
    }

    @Inject(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;clickBlockCreative(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/PlayerControllerMP;" + BLOCK_POS + ENUM_FACING + ")V",
                    shift = At.Shift.AFTER))
    private void onBlockDigged$creative(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Dig-sync--------------------------------------------------
        ZHandle.handle("onBlockDigged", new BlockFace(posBlock, directionFacing));
    }

    @Inject(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;isHittingBlock:Z",
                    ordinal = 0,
                    shift = At.Shift.BEFORE))
    private void isHittingBlock$after(BlockPos posBlock, EnumFacing facing, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Cheat---------------------------------------------
        if (this.isHittingBlock) ZHandle.handle("afterBlockDig");
    }

    @Inject(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;isHittingBlock:Z",
                    ordinal = 1,
                    shift = At.Shift.BEFORE))
    private void isHittingBlock$before(BlockPos posBlock, EnumFacing facing, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Cheat---------------------------------------------
        ZHandle.handle("beforeBlockDig");
    }

    @Inject(method = "onPlayerDamageBlock(" + BLOCK_POS + ENUM_FACING + ")Z",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;blockHitDelay:I",
                    ordinal = 3,
                    shift = At.Shift.AFTER))
    private void onBlockDigged$survival(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> ci) {
        //-ZMod-Dig-sync------------------------------------------
        ZHandle.handle("onBlockDigged", new BlockFace(posBlock, directionFacing));
    }

    @Inject(method = "getBlockReachDistance()F", at = @At("RETURN"))
    private void getBlockReach(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(ZHandle.handle("getPlayerReach", ci.getReturnValueF()));
    }

    @Inject(method = "syncCurrentPlayItem()V",
            at = @At("HEAD"),
            cancellable = true)
    private void allowItemSync(CallbackInfo ci) {
        if (!ZHandle.handle("allowItemSync", true)) ci.cancel();
    }

    @Inject(method = "processRightClickBlock(" + ENTITY_PLAYER_SP + WORLD_CLIENT + BLOCK_POS + ENUM_FACING + VEC3D + ENUM_HAND + ")" + ENUM_ACTION_RESULT,
            at = @At("HEAD"),
            cancellable = true)
    private void checkReachPlace(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand hand, CallbackInfoReturnable<EnumActionResult> ci) {
        if (!ZHandle.handle("checkReachPlace", new BlockFace(stack, pos), true))
            ci.setReturnValue(EnumActionResult.FAIL);

    }

    @Inject(method = "processRightClickBlock(" + ENTITY_PLAYER_SP + WORLD_CLIENT + BLOCK_POS + ENUM_FACING + VEC3D + ENUM_HAND + ")" + ENUM_ACTION_RESULT,
            at = @At("RETURN"))
    private void afterBlockPlace(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand hand, CallbackInfoReturnable<EnumActionResult> ci) {
        ZHandle.handle("afterBlockPlace");
    }

    @Inject(method = "processRightClickBlock(" + ENTITY_PLAYER_SP + WORLD_CLIENT + BLOCK_POS + ENUM_FACING + VEC3D + ENUM_HAND + ")" + ENUM_ACTION_RESULT,
            at = @At(value = "INVOKE",
                    target = SEND_PACKET,
                    shift = At.Shift.BEFORE),
            cancellable = true)
    private void beforeBlockPlace(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand hand, CallbackInfoReturnable<EnumActionResult> ci) {
        ZHandle.handle("beforeBlockPlace");
    }

    @Inject(method = "attackEntity(" + ENTITY_PLAYER + ENTITY + ")V",
            at = @At("HEAD"),
            cancellable = true)
    private void checkReachUse(EntityPlayer playerIn, Entity target, CallbackInfo ci) {
        //-ZMod-Ghost-fix
        if (playerIn == target || !ZHandle.handle("checkReachUse", target, true)) ci.cancel();
    }

    @Inject(method = "interactWithEntity(" + ENTITY_PLAYER + ENTITY + ENUM_HAND + ")" + ENUM_ACTION_RESULT,
            at = @At("HEAD"),
            cancellable = true)
    private void checkReachUse(EntityPlayer player, Entity target, EnumHand hand, CallbackInfoReturnable<EnumActionResult> ci) {
        //-ZMod-?-----------------------------------------------------
        if (!ZHandle.handle("checkReachUse", target, true)) ci.setReturnValue(EnumActionResult.FAIL);
        //------------------------------------------------------------

    }
}

