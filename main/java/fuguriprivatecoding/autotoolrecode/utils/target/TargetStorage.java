package fuguriprivatecoding.autotoolrecode.utils.target;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.RayTrace;

@UtilityClass
public class TargetStorage implements Imports {

    double entityReach = 3, blockReach = 4.5;
    @Getter @Setter EntityLivingBase target;

    public EntityLivingBase getTargetOrSelectedEntity() {
        if (target != null) {
            return target;
        }

        RayTrace rayTrace = RayCastUtils.rayCast(entityReach, blockReach, mc.thePlayer.getRotation());
        if (rayTrace != null && rayTrace.entityHit instanceof EntityLivingBase base) {
            return base;
        }

        return null;
    }

    public EntityLivingBase getSelectedEntity() {
        RayTrace rayTrace = RayCastUtils.rayCast(entityReach, blockReach, mc.thePlayer.getRotation());
        if (rayTrace != null && rayTrace.entityHit instanceof EntityLivingBase base)
            return base;

        return null;
    }
}
