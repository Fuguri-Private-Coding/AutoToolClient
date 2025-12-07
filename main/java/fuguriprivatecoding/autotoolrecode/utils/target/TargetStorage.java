package fuguriprivatecoding.autotoolrecode.utils.target;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@UtilityClass
public class TargetStorage implements Imports {

    double entityReach = 3, blockReach = 4.5;
    @Getter @Setter EntityLivingBase target;

    public EntityLivingBase getTargetOrSelectedEntity() {
        if (target != null) {
            return target;
        }

        MovingObjectPosition movingObjectPosition = RayCastUtils.rayCast(entityReach, blockReach, mc.thePlayer.getRotation());
        if (movingObjectPosition != null && movingObjectPosition.entityHit instanceof EntityLivingBase base) {
            return base;
        }

        return null;
    }

}
