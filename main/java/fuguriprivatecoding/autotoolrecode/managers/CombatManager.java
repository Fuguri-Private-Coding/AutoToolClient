package fuguriprivatecoding.autotoolrecode.managers;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@Getter @Setter
public class CombatManager implements Imports {

    double entityReach = 3, blockReach = 4.5;
    EntityLivingBase target;

    public EntityLivingBase getTargetOrSelectedEntity() {
        if (target != null) {
            return target;
        }

        MovingObjectPosition movingObjectPosition = RayCastUtils.rayCast(entityReach, blockReach, Rot.getServerRotation());
        if (movingObjectPosition != null && movingObjectPosition.entityHit instanceof EntityLivingBase base) {
            return base;
        }

        return null;
    }

}
