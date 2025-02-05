package me.hackclient.utils.rotation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import me.hackclient.utils.Utils;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.optifine.reflect.Reflector;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) { return null; }

        double blockReachDistance = range;

        final Entity renderViewEntity = mc.getRenderViewEntity();
        final Vec3 eyesPosition = renderViewEntity.getPositionEyes(1f);

        MovingObjectPosition mouse = renderViewEntity.rayTrace(range, 1f, rotation);

        Vec3 hittingVector = null;
        final float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        final float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        final float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        final float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);

        final Vec3 entityLook = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
        final Vec3 vector = new Vec3(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance);

        final List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(
                renderViewEntity,
                renderViewEntity.getEntityBoundingBox().addCoord(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance).expand(1d, 1d, 1d),
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
                if (blockReachDistance >= 0d) {
                    hittingVector = movingObjectPosition != null ? movingObjectPosition.hitVec : eyesPosition;
                    pointedEntity = entity;
                    blockReachDistance = 0d;
                }
            } else if (movingObjectPosition != null) {
                final double eyeDistance = eyesPosition.distanceTo(movingObjectPosition.hitVec);

                if (eyeDistance < blockReachDistance || blockReachDistance == 0d) {
                    if (entity == renderViewEntity.ridingEntity) {
                        if (blockReachDistance == 0d) {
                            pointedEntity = entity;
                            hittingVector = movingObjectPosition.hitVec;
                        }
                    } else {
                        hittingVector = movingObjectPosition.hitVec;
                        pointedEntity = entity;
                        blockReachDistance = eyeDistance;
                    }
                }
            }
        }

        if (pointedEntity != null && (blockReachDistance < range || mouse == null)) {
            ClientUtils.chatLog("Pointed entity");
            mouse = new MovingObjectPosition(pointedEntity, hittingVector);
        }

        if (pointedEntity != null && eyesPosition.distanceTo(hittingVector) > range) {
            ClientUtils.chatLog("Missed");
            pointedEntity = null;
            mouse = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, hittingVector, null, new BlockPos(hittingVector));
        }



        return mouse;
    }

    public interface IEntityFilter {
        boolean canRayCast(final Entity entity);
    }
}