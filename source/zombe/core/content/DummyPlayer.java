package zombe.core.content;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import zombe.core.ZHandle;
import zombe.core.ZWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class DummyPlayer extends AbstractClientPlayer {
    public EntityPlayer playerBody;
    public MovementInput movementInput;

    public DummyPlayer(@Nonnull EntityPlayer player) {
        super(getWorld(player), getProfile(player));
        this.playerBody = player;
        this.inventory = player.inventory;
        this.movementInput = new MovementInput();
        this.placeAt(player);
    }

    @Override
    public void moveEntity(@Nonnull MoverType t, double mx, double my, double mz) {
        if (this == getView()) {
            ZHandle.handle("beforeViewMove", new Vec3d(mx, my, mz));
            Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
            super.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            ZHandle.handle("afterViewMove", motion);
        } else {
            super.moveEntity(t, mx, my, mz);
        }
    }

    /** Performs a ray trace for the distance specified and using the partial tick time. Args: distance, partialTickTime */
    @Override
    public RayTraceResult rayTrace(double blockReachDistance, float partialTicks) {
        RayTraceResult trace = super.rayTrace(blockReachDistance, partialTicks);
        return (RayTraceResult) ZHandle.handle("onPlayerRayTrace", trace);
    }

    /** Adds velocity to push the entity out of blocks at the specified x, y, z position */
    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        if (this.noClip) {
            return false;
        }

        BlockPos var7 = new BlockPos(x, y, z);
        /// TODO wont' this always be zero? + better variable names
        double dx = x - (double) getX(var7);
        double dz = z - (double) getZ(var7);

        if (!this.isMostlyEmpty(var7)) {
            byte var12 = -1;
            double var13 = 9999.0D;

            if (this.isMostlyEmpty(var7.offset(EnumFacing.WEST)) && dx < var13) {
                var13 = dx;
                var12 = 0;
            }

            if (this.isMostlyEmpty(var7.offset(EnumFacing.EAST)) && 1.0D - dx < var13) {
                var13 = 1.0D - dx;
                var12 = 1;
            }

            if (this.isMostlyEmpty(var7.offset(EnumFacing.NORTH)) && dz < var13) {
                var13 = dz;
                var12 = 4;
            }

            if (this.isMostlyEmpty(var7.offset(EnumFacing.SOUTH)) && 1.0D - dz < var13) {
                // Never used...
                //var13 = 1.0D - dz;
                var12 = 5;
            }

            double var15 = 0.1;

            switch (var12) {
                case 0:
                    this.motionX = -var15;
                    break;
                case 1:
                    this.motionX = var15;
                    break;
                case 4:
                    this.motionZ = -var15;
                    break;
                case 5:
                    this.motionZ = var15;
                    break;
            }
        }

        return false;
    }

    @Override
    public void addChatMessage(@Nonnull ITextComponent message) { }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, @Nullable String commandName) {
        return false;
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
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
    public void updateEntityActionState() {
        super.updateEntityActionState();
        assert this.movementInput != null;

        this.moveStrafing = this.movementInput.moveStrafe;
        this.moveForward = this.movementInput.moveForward;
        this.isJumping = this.movementInput.jump;
    }

    @Override
    public void onLivingUpdate() {
        assert this.movementInput != null;

        this.inPortal = false;
        this.movementInput.updatePlayerMoveState();

        this.pushOutOfBlocks(this.posX - this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ + this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ - this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ - this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + this.width * 0.35D, getAABB(this).minY + 0.5D, this.posZ + this.width * 0.35D);

        super.onLivingUpdate();
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
        return false;
    }

    /** Checks if this entity is inside of an opaque block */
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    @Override
    public void jump() {
        super.jump();
        if (this == getView()) {
            ZHandle.handle("onPlayerJump", this);
        }
    }

    /** Gets the player's field of view multiplier. (ex. when flying) */
    public float getFOVMultiplier() {
        return this.getFovModifier();
    }

    private boolean isMostlyEmpty(@Nonnull BlockPos p) {
        IBlockState block = getStateAt(getWorld(this), p);
        IBlockState above = getStateAt(getWorld(this), p.offset(EnumFacing.UP));
        return !block.isNormalCube() && !above.isNormalCube();
    }

    @Override
    public boolean isSpectator() {
        if (ZHandle.handle("isNoclip", this, false)) {
            ZWrapper.setNoclip(this, true);
        }

        return super.isSpectator();
    }

    @Override
    public float getFovModifier() {
        return 1.0F;
    }

    public void placeAt(@Nonnull Entity ent) {
        //placeAt(ent.posX, getAABB(ent).minY + getYOffset(this) - this.ySize, ent.posZ);
        this.placeAt(getX(ent), getY(ent), getZ(ent));
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

    @Nonnull
    @Override
    public ItemStack getHeldItem(@Nonnull EnumHand hand) {
        return this.playerBody.getHeldItem(hand);
    }

    @Override
    //public boolean isClientWorld() { return true; } // < 1.8 MCP 9.10
    public boolean isServerWorld() {
        return true;
    }
}

