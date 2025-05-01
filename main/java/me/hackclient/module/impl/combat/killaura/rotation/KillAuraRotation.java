package me.hackclient.module.impl.combat.killaura.rotation;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;


@Getter
@Setter
public abstract class KillAuraRotation implements InstanceAccess {
    Vec3 offset = new Vec3(0,0,0);

    public abstract Rotation compute(final Rotation startsFrom, final EntityLivingBase target, final float simpleYawSpeed, final float simplePitchSpeed, final float smooth, final float yawMultiplier, final float pitchMultiplier);

}
