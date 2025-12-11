package net.minecraft.util;

import lombok.Getter;
import net.minecraft.entity.Entity;

public class RayTrace {
    @Getter private BlockPos blockPos;
    public RayType typeOfHit;
    public EnumFacing sideHit;
    public Vec3 hitVec;
    public Entity entityHit;

    public RayTrace(Vec3 hitVecIn, EnumFacing facing, BlockPos blockPosIn) {
        this(RayType.BLOCK, hitVecIn, facing, blockPosIn);
    }

    public RayTrace(Vec3 p_i45552_1_, EnumFacing facing) {
        this(RayType.BLOCK, p_i45552_1_, facing, BlockPos.ORIGIN);
    }

    public RayTrace(Entity entityIn) {
        this(entityIn, new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ));
    }

    public RayTrace(RayType typeOfHitIn, Vec3 hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
        this.typeOfHit = typeOfHitIn;
        this.blockPos = blockPosIn;
        this.sideHit = sideHitIn;
        this.hitVec = new Vec3(hitVecIn.xCoord, hitVecIn.yCoord, hitVecIn.zCoord);
    }

    public RayTrace(Entity entityHitIn, Vec3 hitVecIn) {
        this.typeOfHit = RayType.ENTITY;
        this.entityHit = entityHitIn;
        this.hitVec = hitVecIn;
    }

    public String toString() {
        return "HitResult{type=" + this.typeOfHit + ", blockpos=" + this.blockPos + ", f=" + this.sideHit + ", pos=" + this.hitVec + ", entity=" + this.entityHit + '}';
    }

    public enum RayType {
        MISS,
        BLOCK,
        ENTITY
    }
}
