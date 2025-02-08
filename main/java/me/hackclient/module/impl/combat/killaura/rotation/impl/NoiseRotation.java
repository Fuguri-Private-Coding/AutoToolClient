package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class NoiseRotation extends KillAuraRotation {

    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, float simpleYawSpeed, float simplePitchSpeed) {
        final double hTemperature = 0.003;
        final double vTemperature = 0.003;

        AxisAlignedBB box = target.getExpandedBoundingBox();
        Vec3 vec = RotationUtils.getBestHitVec(target.getPositionEyes(1f).addVector(
                Math.sin(System.currentTimeMillis() * hTemperature) * box.getLengthX(),
                Math.cos(System.currentTimeMillis() * vTemperature) * box.getLengthY(),
                Math.sin(System.currentTimeMillis() * hTemperature) * box.getLengthZ()
        ), box);

        Delta delta = RotationUtils.getDeltaToPoint(startsFrom, vec).limit(simpleYawSpeed, simplePitchSpeed).divine(2f, 2f);

        if (Math.abs(mc.thePlayer.getPositionEyes(1.0f).yCoord - target.getPositionEyes(1.0f).yCoord) < 1) {
            delta.setPitch(0);
        }

        return startsFrom.add(delta).fix();
    }
}
