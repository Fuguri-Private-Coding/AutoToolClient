package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class IntaveNewRotation extends KillAuraRotation {

    private final Rotation lastDelta = new Rotation();

    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, float simpleYawSpeed, float simplePitchSpeed, float accelSlowDown, int yawAccelSpeed, int pitchAccelSpeed, float smooth) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * 0.4,
                box.minY + box.getLengthY() * 0.5,
                box.minZ + box.getLengthZ() * 0.4,
                box.minX + box.getLengthX() * 0.6,
                box.minY + box.getLengthY() * 0.8,
                box.minZ + box.getLengthZ() * 0.6
        );

        Rotation rots = RotationUtils.getRotationNearest(startsFrom, box);
        Rotation delta = new Rotation(MathHelper.wrapDegree(rots.getYaw() - startsFrom.getYaw()), rots.getPitch() - startsFrom.getPitch());

        lastDelta.setYaw(Math.clamp(lastDelta.getYaw() * (1 - accelSlowDown), -yawAccelSpeed, yawAccelSpeed));
        lastDelta.setPitch(Math.clamp(lastDelta.getPitch() * (1 - accelSlowDown), -pitchAccelSpeed, pitchAccelSpeed));

        delta.setYaw(Math.clamp(delta.getYaw(), -simpleYawSpeed, simpleYawSpeed));
        delta.setPitch(Math.clamp(delta.getPitch(), -simplePitchSpeed, simplePitchSpeed));

        delta.setYaw(delta.getYaw() / smooth);
        delta.setPitch(delta.getPitch() / smooth);

        delta.setYaw(delta.getYaw() + lastDelta.getYaw());
        delta.setPitch(delta.getPitch() + lastDelta.getPitch());

        float gcd = RotationUtils.getMouseGCD();

        delta.setYaw(Math.round(delta.getYaw() / gcd) * gcd);
        delta.setPitch(Math.round(delta.getPitch() / gcd) * gcd);

        lastDelta.setYaw(delta.getYaw());
        lastDelta.setPitch(delta.getPitch());

        return new Rotation(startsFrom.getYaw() + delta.getYaw(), Math.clamp(startsFrom.getPitch() + delta.getPitch(), -90, 90));
    }
}
