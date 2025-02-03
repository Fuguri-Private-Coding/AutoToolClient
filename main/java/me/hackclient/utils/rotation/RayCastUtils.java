package me.hackclient.utils.rotation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import me.hackclient.utils.Utils;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.optifine.reflect.Reflector;

import java.util.List;

public final class RayCastUtils implements InstanceAccess {

    public static Entity raycastEntity(final double range, final IEntityFilter entityFilter) {
        return raycastEntity(range, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(),
                entityFilter);
    }

    public static Entity raycastEntity(final double range, final float yaw, final float pitch, final IEntityFilter entityFilter) {
        final Entity renderViewEntity = mc.getRenderViewEntity();

        if (renderViewEntity != null && mc.theWorld != null) {
            double blockReachDistance = range;
            final Vec3 eyePosition = renderViewEntity.getPositionEyes(1F);

            final float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            final float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            final float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
            final float pitchSin = MathHelper.sin(-pitch * 0.017453292F);

            final Vec3 entityLook = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
            final Vec3 vector = eyePosition.addVector(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance);
            final List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(renderViewEntity, renderViewEntity.getEntityBoundingBox().addCoord(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance).expand(1D, 1D, 1D), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity :: canBeCollidedWith));

            Entity pointedEntity = null;

            for (final Entity entity : entityList) {
                if (!entityFilter.canRayCast(entity))
                    continue;

                final float collisionBorderSize = entity.getCollisionBorderSize();
                final AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                final MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector);

                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance >= 0.0D) {
                        pointedEntity = entity;
                        blockReachDistance = 0.0D;
                    }
                } else if (movingObjectPosition != null) {
                    final double eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec);

                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0D) {
                        if (entity == renderViewEntity.ridingEntity) {
                            if (blockReachDistance == 0.0D)
                                pointedEntity = entity;
                        } else {
                            pointedEntity = entity;
                            blockReachDistance = eyeDistance;
                        }
                    }
                }
            }

            return pointedEntity;
        }

        return null;
    }

    public static Entity raycastEntityFromPos(final Vec3 pos, final double range, final float yaw, final float pitch, final IEntityFilter entityFilter) {
        final Entity renderViewEntity = mc.getRenderViewEntity();

        if (renderViewEntity != null && mc.theWorld != null) {
            double blockReachDistance = range;
            final Vec3 eyePosition = pos;

            final float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            final float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            final float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
            final float pitchSin = MathHelper.sin(-pitch * 0.017453292F);

            final Vec3 entityLook = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
            final Vec3 vector = eyePosition.addVector(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance);
            final List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(renderViewEntity, renderViewEntity.getEntityBoundingBox().addCoord(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance).expand(1D, 1D, 1D), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity :: canBeCollidedWith));

            Entity pointedEntity = null;

            for (final Entity entity : entityList) {
                if (!entityFilter.canRayCast(entity))
                    continue;

                final float collisionBorderSize = entity.getCollisionBorderSize();
                final AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                final MovingObjectPosition movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector);

                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance >= 0.0D) {
                        pointedEntity = entity;
                        blockReachDistance = 0.0D;
                    }
                } else if (movingObjectPosition != null) {
                    final double eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec);

                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0D) {
                        if (entity == renderViewEntity.ridingEntity) {
                            if (blockReachDistance == 0.0D)
                                pointedEntity = entity;
                        } else {
                            pointedEntity = entity;
                            blockReachDistance = eyeDistance;
                        }
                    }
                }
            }

            return pointedEntity;
        }

        return null;
    }

    public static Entity raycastEntityFromPos(final Vec3 pos, final double range, final IEntityFilter entityFilter) {
        return raycastEntityFromPos(pos, range, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entityFilter);
    }

    public static MovingObjectPosition rayCast(double range, Rotation rotation) {
        if (!Utils.isWorldLoaded() || mc.getRenderViewEntity() == null) { return null; }

        Entity entity = mc.getRenderViewEntity();
        MovingObjectPosition mouseOver = entity.rayTrace(range, 1f, rotation);

        Vec3 eyes = entity.getPositionEyes(1f);
        Vec3 look = entity.getVectorForRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 extendedLook = new Vec3(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(extendedLook.xCoord, extendedLook.yCoord, extendedLook.zCoord).expand(1f, 1f, 1f), entity1 -> entity1.canBeCollidedWith() && (!(entity1 instanceof EntityPlayer player) || !player.isSpectator()));

        Vec3 vec = null;
        Entity pointedEntity = null;

        double d1 = range;

        if (mouseOver != null) {
            d1 = mouseOver.hitVec.distanceTo(eyes);
        }

        for (Entity ent : list) {
            double d2;
            float f1 = ent.getCollisionBorderSize();
            AxisAlignedBB box = ent.getEntityBoundingBox().expand(f1, f1, f1);
            MovingObjectPosition movingObjectPosition = box.calculateIntercept(eyes, extendedLook);

            if (box.isVecInside(eyes)) {
                pointedEntity = ent;
                vec = movingObjectPosition == null ? eyes : movingObjectPosition.hitVec;
                d1 = 0D;
                continue;
            }

            if (movingObjectPosition == null || !((d2 = eyes.distanceTo(movingObjectPosition.hitVec)) < d1) && d1 != 0.0) continue;

            boolean flag2 = false;
            if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                flag2 = Reflector.callBoolean(ent, Reflector.ForgeEntity_canRiderInteract);
            }

            if (ent != entity.ridingEntity || flag2) {
                pointedEntity = ent;
                vec = movingObjectPosition.hitVec;
                d1 = d2;
            }

            if (d1 != 0D) continue;

            pointedEntity = ent;
            vec = movingObjectPosition.hitVec;
        }
        if (pointedEntity != null && eyes.distanceTo(vec) > range) {
            pointedEntity = null;
            mouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec, null, new BlockPos(vec));
        }
        if (pointedEntity != null && (d1 < range || mouseOver == null)) {
            mouseOver = new MovingObjectPosition(pointedEntity, vec);
        }

        return mouseOver;
    }

    public interface IEntityFilter {
        boolean canRayCast(final Entity entity);
    }
}