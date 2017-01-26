package zombe.core.content;

import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import zombe.core.ZHandle;
import zombe.core.ZWrapper;

import static zombe.core.ZWrapper.*;

public final class DummyPlayer extends AbstractClientPlayer {
    public EntityPlayer playerBody;
    public MovementInput movementInput;

    public DummyPlayer(EntityPlayer player) {
        super(getWorld(player), getProfile(player));
        this.playerBody = player;
        this.inventory = player.inventory;
        this.movementInput = new MovementInput();
        placeAt(player);
    }

    @Override
    public void moveEntity(MoverType t, double mx, double my, double mz) {
        if (this == getView()) {
            ZHandle.handle("beforeViewMove", new Vec3d(mx,my,mz));
            Vec3d motion =  new Vec3d(this.motionX, this.motionY, this.motionZ);
            super.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            ZHandle.handle("afterViewMove", motion);
        } else {
            super.moveEntity(t, mx, my, mz);
        }
    }

    @Override
    public void onUpdate() {
        setLastX(this, getX(this));
        setLastY(this, getY(this));
        setLastZ(this, getZ(this));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        ZHandle.handle("onViewUpdate", this);
        super.onUpdate();
    }

    @Override
    public void jump() {
        super.jump();
        if (this == getView()) ZHandle.handle("onPlayerJump", this);
    }

    @Override
    public void onLivingUpdate() {
        this.inPortal = false;
        this.movementInput.updatePlayerMoveState();

        this.pushOutOfBlocks(this.posX - this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ + this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ - this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ - this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ + this.width * 0.35D);

        super.onLivingUpdate();
    }

    /**
     * Gets the player's field of view multiplier. (ex. when flying)
     */
    public float getFOVMultiplier() {
        return getFovModifier();
    }
    @Override
    public float getFovModifier() {
        return 1.0F;
    }

    /**
      * Checks if this entity is inside of an opaque block
     */
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    private boolean isMostlyEmpty(BlockPos p) {
        Block block = getBlockAt(getWorld(this),p);
        return !block.isNormalCube(block.getDefaultState())
            && !getBlockAt(getWorld(this), p.offset(EnumFacing.UP)).isNormalCube(null);
    }

    /**
     * Adds velocity to push the entity out of blocks at the specified x, y, z position Args: x, y, z
     */
    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (this.noClip) return false;

        BlockPos var7 = new BlockPos(x, y, z);
        double var8 = x - (double)getX(var7);
        double var10 = z - (double)getZ(var7);

        if (!isMostlyEmpty(var7)) {
            byte var12 = -1;
            double var13 = 9999.0D;

            if (isMostlyEmpty(var7.offset(EnumFacing.WEST)) && var8 < var13) {
                var13 = var8;
                var12 = 0;
            }

            if (isMostlyEmpty(var7.offset(EnumFacing.EAST)) && 1.0D - var8 < var13) {
                var13 = 1.0D - var8;
                var12 = 1;
            }

            if (isMostlyEmpty(var7.offset(EnumFacing.NORTH)) && var10 < var13) {
                var13 = var10;
                var12 = 4;
            }

            if (isMostlyEmpty(var7.offset(EnumFacing.SOUTH)) && 1.0D - var10 < var13) {
                // Never used...
                //var13 = 1.0D - var10;
                var12 = 5;
            }

            double var15 = 0.1;

            if (var12 == 0) this.motionX = -var15;
            if (var12 == 1) this.motionX =  var15;
            if (var12 == 4) this.motionZ = -var15;
            if (var12 == 5) this.motionZ =  var15;
        }

        return false;
    }

    /**
     * Performs a ray trace for the distance specified and using the partial tick time. Args: distance, partialTickTime
     */
    @Override
    public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
        RayTraceResult trace = super.rayTrace(blockReachDistance, partialTicks);
        return (RayTraceResult) ZHandle.handle("onPlayerRayTrace", trace);
    }

    @Override
    public boolean isSpectator() {
        if (ZHandle.handle("isNoclip",this,false)) ZWrapper.setNoclip(this,true);
        return super.isSpectator();
    }

    public void placeAt(Entity ent) {
        //placeAt(ent.posX, getAABB(ent).minY + getYOffset(this) - this.ySize, ent.posZ);
        placeAt(getX(ent), getY(ent), getZ(ent));
        this.rotationYaw = ent.rotationYaw;
        this.rotationPitch = ent.rotationPitch;
        this.prevRotationYaw = ent.prevRotationYaw;
        this.prevRotationPitch = ent.prevRotationPitch;
        this.cameraYaw = 0;
        this.cameraPitch = 0;
    }

    public void placeAt(double x, double y, double z) {
        ZWrapper.setPositions(this, x, y, z);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    @Override
    public ItemStack getHeldItem(EnumHand hand) {
        return playerBody.getHeldItem(hand);
    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    @Override
    public void updateEntityActionState() {
        super.updateEntityActionState();
        this.moveStrafing = this.movementInput.moveStrafe;
        this.moveForward = this.movementInput.moveForward;
        this.isJumping = this.movementInput.jump;
    }

    @Override
    //public boolean isClientWorld() { return true; } // < 1.8 MCP 9.10
    public boolean isServerWorld() { return true; }
    @Override
    public void addChatMessage(ITextComponent message) {}
    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2Str) { return false; }
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) { return false; }
}

