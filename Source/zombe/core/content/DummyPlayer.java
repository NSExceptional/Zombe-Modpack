package zombe.core.content;

import zombe.core.*;
import static zombe.core.ZWrapper.*;
import zombe.core.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

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
    public void moveEntity(double mx, double my, double mz) {
        if (this == getView()) {
            ZHandle.handle("beforeViewMove", new Vec3(mx,my,mz));
            Vec3 motion = new Vec3(this.motionX, this.motionY, this.motionZ);
            super.moveEntity(this.motionX, this.motionY, this.motionZ);
            ZHandle.handle("afterViewMove", motion);
        } else {
            super.moveEntity(mx, my, mz);
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
        return func_175156_o();
    }
    @Override
    public float func_175156_o() {
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
        return !getBlockAt(getWorld(this),p).isNormalCube() 
            && !getBlockAt(getWorld(this),p.offsetUp()).isNormalCube();
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

            if (isMostlyEmpty(var7.offsetWest()) && var8 < var13) {
                var13 = var8;
                var12 = 0;
            }

            if (isMostlyEmpty(var7.offsetEast()) && 1.0D - var8 < var13) {
                var13 = 1.0D - var8;
                var12 = 1;
            }

            if (isMostlyEmpty(var7.offsetNorth()) && var10 < var13) {
                var13 = var10;
                var12 = 4;
            }

            if (isMostlyEmpty(var7.offsetSouth()) && 1.0D - var10 < var13) {
                var13 = 1.0D - var10;
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
    public MovingObjectPosition func_174822_a(double distance, float delta) {
        return rayTrace(distance, delta);
    }
    public MovingObjectPosition superRayTrace(double distance, float delta) {
        return super.func_174822_a(distance, delta);
    }
    public MovingObjectPosition rayTrace(double distance, float delta) {
        MovingObjectPosition mop = superRayTrace(distance, delta);
        if (this == getView())
            return (MovingObjectPosition) ZHandle.handle("onViewRayTrace", mop);
        return mop;
    }

    @Override
    public boolean func_175149_v() {
        if (ZHandle.handle("isNoclip",this,false)) ZWrapper.setNoclip(this,true);
        return super.func_175149_v();
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
    public ItemStack getHeldItem() {
        return playerBody.getHeldItem();
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
    public void addChatMessage(IChatComponent message) {}
    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2Str) { return false; }
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) { return false; }
}

