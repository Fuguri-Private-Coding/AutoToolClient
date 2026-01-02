package fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace;

import com.google.common.base.Predicates;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.joml.Vector2f;

import java.util.List;

public class RayCastUtils implements Imports {

    public static Entity raycastEntity(final double range, final IEntityFilter entityFilter) {
        return raycastEntity(range, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
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
                final RayTrace rayTrace = axisAlignedBB.calculateIntercept(eyePosition, vector);

                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance >= 0.0D) {
                        pointedEntity = entity;
                        blockReachDistance = 0.0D;
                    }
                } else if (rayTrace != null) {
                    final double eyeDistance = eyePosition.distanceTo(rayTrace.hitVec);

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

    public static RayTrace rayCast(Vec3 eyesPosition, final double entityRange, final double blockRange, Rot rotation) {
        return rayCast(eyesPosition, entityRange, blockRange, rotation, 1.0f);
    }

    public static RayTrace rayCast(Vec3 eyesPosition, final double entityRange, final double blockRange, Rot rotation, float partialTicks) {
        if (mc.theWorld == null || mc.getRenderViewEntity() == null) { return null; }

        double blockReachDistance = blockRange;
        double entityBlockReach = entityRange;

        final Entity renderViewEntity = mc.getRenderViewEntity();

        RayTrace mouse = renderViewEntity.rayTrace(blockReachDistance, partialTicks, rotation);

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
            final RayTrace rayTrace = hitBox.calculateIntercept(eyesPosition, vector);

            if (hitBox.isVecInside(eyesPosition)) {
                if (entityBlockReach >= 0d) {
                    hittingVector = rayTrace != null ? rayTrace.hitVec : eyesPosition;
                    pointedEntity = entity;
                    entityBlockReach = 0d;
                }
            } else if (rayTrace != null) {
                final double eyeDistance = eyesPosition.distanceTo(rayTrace.hitVec);

                if (eyeDistance < entityBlockReach || entityBlockReach == 0d) {
                    if (entity == renderViewEntity.ridingEntity) {
                        if (entityBlockReach == 0d) {
                            pointedEntity = entity;
                            hittingVector = rayTrace.hitVec;
                        }
                    } else {
                        hittingVector = rayTrace.hitVec;
                        pointedEntity = entity;
                        entityBlockReach = eyeDistance;
                    }
                }
            }
        }


        if (pointedEntity != null && (entityBlockReach < entityRange || mouse == null)) {
            mouse = new RayTrace(pointedEntity, hittingVector);
        }

        if (pointedEntity != null && eyesPosition.distanceTo(hittingVector) > entityBlockReach) {
            mouse = new RayTrace(RayTrace.RayType.MISS, hittingVector, null, new BlockPos(hittingVector));
        }

        return mouse;
    }

    public static RayTrace rayCast(final double entityRange, final double blockRange, Rot rotation) {
        return rayCast(mc.thePlayer.getPositionEyes(1f), entityRange, blockRange, rotation);
    }


    public static RayTrace rayCast(final Rot rotation, final double range) {
        return rayCast(new Vector2f(rotation.getYaw(), rotation.getPitch()), range, 0);
    }

    public static RayTrace rayCast(final Rot rotation, final double range, final float expand) {
        return rayCast(new Vector2f(rotation.getYaw(), rotation.getPitch()), range, expand);
    }

    public static RayTrace rayCast(final Vector2f rotation, final double range, final float expand) {
        return rayCast(rotation, range, expand, mc.thePlayer);
    }

    public static RayTrace rayCast(final Vector2f rotation, final double range, final float expand, Entity entity) {
        final float partialTicks = mc.timer.renderPartialTicks;
        RayTrace objectMouseOver;

        if (entity != null && mc.theWorld != null) {
            objectMouseOver = entity.rayTrace(range, 1f, new Rot(rotation.x, rotation.y));
            double d1 = range;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = mc.thePlayer.getVectorForRotation(rotation.y, rotation.x);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize() + expand;
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final RayTrace movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new RayTrace(pointedEntity, vec33);
            }

            return objectMouseOver;
        }

        return null;
    }

    public static boolean overBlock(final Vector2f rotation, final EnumFacing enumFacing, final BlockPos pos, final boolean strict) {
        final RayTrace rayTrace = mc.thePlayer.rayTrace(4.5f, mc.timer.renderPartialTicks, new Rot(rotation.x, rotation.y));

        if (rayTrace == null) return false;

        final Vec3 hitVec = rayTrace.hitVec;
        if (hitVec == null) return false;

        return rayTrace.getBlockPos().equals(pos) && (!strict || rayTrace.sideHit == enumFacing);
    }

    public static boolean overBlock(final EnumFacing enumFacing, final BlockPos pos, final boolean strict) {
        final RayTrace rayTrace = mc.objectMouseOver;

        if (rayTrace == null) return false;

        final Vec3 hitVec = rayTrace.hitVec;
        if (hitVec == null) return false;

        return rayTrace.getBlockPos().equals(pos) && (!strict || rayTrace.sideHit == enumFacing);
    }

    public static Boolean overBlock(final Vector2f rotation, final BlockPos pos) {
        return overBlock(rotation, EnumFacing.UP, pos, false);
    }

    public static Boolean overBlock(final Vector2f rotation, final BlockPos pos, final EnumFacing enumFacing) {
        return overBlock(rotation, enumFacing, pos, true);
    }

    public interface IEntityFilter {
        boolean canRayCast(final Entity entity);
    }
}