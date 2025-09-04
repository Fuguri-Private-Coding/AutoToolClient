package fuguriprivatecoding.autotoolrecode.utils.raytrace;

import com.google.common.base.Predicates;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.List;

public class RayCastUtils implements Imports {

    public static Entity raycastEntity(final double range, final IEntityFilter entityFilter) {
        return raycastEntity(range, Rot.getServerRotation().getYaw(), Rot.getServerRotation().getPitch(),
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
        return raycastEntityFromPos(pos, range, Rot.getServerRotation().getYaw(), Rot.getServerRotation().getPitch(), entityFilter);
    }

    public static MovingObjectPosition rayCast(Vec3 eyesPosition, final double entityRange, final double blockRange, Rot rotation) {
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) { return null; }

        double blockReachDistance = blockRange;
        double entityBlockReach = entityRange;

        final Entity renderViewEntity = mc.getRenderViewEntity();

        MovingObjectPosition mouse = renderViewEntity.rayTrace(blockReachDistance, mc.timer.renderPartialTicks, rotation);

        Vec3 hittingVector = null;
        final float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        final float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        final float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        final float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);

        final Vec3 entityLook = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
        final Vec3 vector = eyesPosition.addVector(entityLook.xCoord * entityBlockReach, entityLook.yCoord * entityBlockReach, entityLook.zCoord * entityBlockReach);

        final List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(
                renderViewEntity,
                renderViewEntity.getEntityBoundingBox().addCoord(entityLook.xCoord * entityBlockReach, entityLook.yCoord * entityBlockReach, entityLook.zCoord * entityBlockReach).expand(1d, 1d, 1d),
                Predicates.and(EntitySelectors.NOT_SPECTATING, Entity :: canBeCollidedWith)
        );

        Entity pointedEntity = null;

        if (mouse != null) {
            blockReachDistance = eyesPosition.distanceTo(mouse.hitVec);
        }

        for (Entity entity : entityList) {
            final float borderSize = entity.getCollisionBorderSize();
            final AxisAlignedBB hitBox = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
            final MovingObjectPosition movingObjectPosition = hitBox.calculateIntercept(eyesPosition, vector);

            if (hitBox.isVecInside(eyesPosition)) {
                if (entityBlockReach >= 0d) {
                    hittingVector = movingObjectPosition != null ? movingObjectPosition.hitVec : eyesPosition;
                    pointedEntity = entity;
                    entityBlockReach = 0d;
                }
            } else if (movingObjectPosition != null) {
                final double eyeDistance = eyesPosition.distanceTo(movingObjectPosition.hitVec);

                if (eyeDistance < entityBlockReach || entityBlockReach == 0d) {
                    if (entity == renderViewEntity.ridingEntity) {
                        if (entityBlockReach == 0d) {
                            pointedEntity = entity;
                            hittingVector = movingObjectPosition.hitVec;
                        }
                    } else {
                        hittingVector = movingObjectPosition.hitVec;
                        pointedEntity = entity;
                        entityBlockReach = eyeDistance;
                    }
                }
            }
        }


        if (pointedEntity != null && (entityBlockReach < entityRange || mouse == null)) {
            mouse = new MovingObjectPosition(pointedEntity, hittingVector);
        }

        if (pointedEntity != null && eyesPosition.distanceTo(hittingVector) > entityBlockReach) {
            mouse = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, hittingVector, null, new BlockPos(hittingVector));
        }

        return mouse;
    }

    public static MovingObjectPosition rayCast(final double entityRange, final double blockRange, Rot rotation) {
        return rayCast(mc.thePlayer.getPositionEyes(1f), entityRange, blockRange, rotation);
    }

    public interface IEntityFilter {
        boolean canRayCast(final Entity entity);
    }
}