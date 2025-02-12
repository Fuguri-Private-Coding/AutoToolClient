package me.hackclient.managers;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@Getter @Setter
public class CombatManager implements InstanceAccess {
    double entityReach = 3, blockReach = 4.5;
    EntityLivingBase target;

    public EntityLivingBase getTargetOrSelectedEntity() {
        if (target != null) {
            return target;
        }

        MovingObjectPosition movingObjectPosition = RayCastUtils.rayCast(entityReach, blockReach, Rotation.getServerRotation());
        if (movingObjectPosition != null && movingObjectPosition.entityHit instanceof EntityLivingBase base) {
            return base;
        }

        return null;
    }
}
