package me.hackclient.utils.raytrace;

import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.optifine.reflect.Reflector;
import java.util.List;

@UtilityClass
public class RayTraceUtils implements Imports {
    public MovingObjectPosition rayTrace(final Vec3 eyesPos, final double entityRange, final double blockRange, Rotation rotation) {
        final Entity renderViewEntity = mc.getRenderViewEntity();

        if (renderViewEntity == null) {
            return null;
        }

        Entity pointedEntity = null;
        Vec3 hittingVec = null;

        double entityReach = entityRange;

        final Vec3 entityLook = renderViewEntity.getLook(rotation);
        final Vec3 extendedLook = new Vec3(entityLook.xCoord * entityReach, entityLook.yCoord * entityReach, entityLook.zCoord * entityReach);
        final Vec3 vector = eyesPos.add(extendedLook);

        MovingObjectPosition mouse = mc.theWorld.rayTraceBlocks(eyesPos, new Vec3(entityLook.xCoord * blockRange, entityLook.yCoord * blockRange, entityLook.zCoord * blockRange), false, false, true);

        float f = 1;
        List<Entity> entities = mc.theWorld.getEntitiesInAABBexcluding(
                renderViewEntity,
                renderViewEntity.getEntityBoundingBox().addCoord(extendedLook.xCoord, extendedLook.yCoord, extendedLook.zCoord).expand(f, f, f), 
                Predicates.and(EntitySelectors.NOT_SPECTATING, Entity :: canBeCollidedWith)
        );

        for (Entity entity : entities) {
            final float expand = entity.getCollisionBorderSize();
            final AxisAlignedBB hitBox = entity.getEntityBoundingBox().expand(expand, expand, expand);
            final MovingObjectPosition movingObjectPosition = hitBox.calculateIntercept(eyesPos, vector);

            if (hitBox.isVecInside(eyesPos)) {
                if (entityReach >= 0) {
                    entityReach = 0;
                    pointedEntity = entity;
                    hittingVec = movingObjectPosition != null ? movingObjectPosition.hitVec : eyesPos;
                }
            } else if (movingObjectPosition != null) {
                double distance = eyesPos.distanceTo(movingObjectPosition.hitVec);

                if (distance < entityReach || entityReach == 0) {
                    boolean flag = false;

                    if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                        flag = Reflector.callBoolean(entity, Reflector.ForgeEntity_canRiderInteract);
                    }

                    if (!flag && entity == renderViewEntity.ridingEntity) {
                        if (entityReach == 0) {
                            pointedEntity = entity;
                            hittingVec = movingObjectPosition.hitVec;
                        }
                    } else {
                        pointedEntity = entity;
                        hittingVec = movingObjectPosition.hitVec;
                        entityReach = distance;
                    }
                }
            }
        }

        if (pointedEntity != null && eyesPos.distanceTo(hittingVec) > entityRange) {
            pointedEntity = null;
            mouse = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, hittingVec, null, new BlockPos(hittingVec));
        }

        if (pointedEntity != null && (entityReach < entityRange || mouse == null)) {
            mouse = new MovingObjectPosition(pointedEntity, hittingVec);
        }

        return mouse;
    }
}
