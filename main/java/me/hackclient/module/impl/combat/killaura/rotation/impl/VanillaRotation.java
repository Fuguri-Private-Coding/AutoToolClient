package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class VanillaRotation extends KillAuraRotation {

    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, float simpleYawSpeed, float simplePitchSpeed, float smooth, float yawMultiplier, float pitchMultiplier) {
        Vec3 needVec = RotationUtils.getBestHitVec(target).add(getOffset());
        Delta delta = RotationUtils.getDeltaToPoint(startsFrom, needVec);

        delta.setYaw(MathHelper.clamp(delta.getYaw(), -simpleYawSpeed, simpleYawSpeed));
        delta.setPitch(MathHelper.clamp(delta.getPitch() , -simplePitchSpeed, simplePitchSpeed));

        delta = RotationUtils.fixDelta(delta);

        return new Rotation(
                startsFrom.getYaw() + delta.getYaw(),
                MathHelper.clamp(startsFrom.getPitch() + delta.getPitch(), -90, 90)
        );
    }
}
