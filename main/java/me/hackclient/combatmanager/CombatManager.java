package me.hackclient.combatmanager;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.entity.EntityLivingBase;

@Getter @Setter
public class CombatManager implements InstanceAccess {
    double reach = 3;
    EntityLivingBase target;

    public EntityLivingBase getTargetOrSelectedEntity() {
        if (target != null) {
            return target;
        }

        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityLivingBase base) {
            return base;
        }

        return null;
    }
}
