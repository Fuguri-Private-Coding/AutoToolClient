package me.hackclient.module.impl.combat.killaura.rotation;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public abstract class KillAuraRotation {
    @Getter @Setter Vec3 offset;

    public abstract Rotation compute(final Rotation startsFrom, final EntityLivingBase target, final float simpleYawSpeed, final float simplePitchSpeed);
}
