package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class TestRotation extends KillAuraRotation implements InstanceAccess {


    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, float simpleYawSpeed, float simplePitchSpeed) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        if (box.isVecInside(mc.thePlayer.getPositionEyes(1.0f))) {
            return startsFrom.copy();
        }

        double distance = DistanceUtils.getDistanceToEntity(target);

        if (distance > 3) {
            distance = 3;
        }

        double govnoFactor = distance / 3D;

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * 0.4,
                box.minY + box.getLengthY() * 0.0,
                box.minZ + box.getLengthZ() * 0.4,
                box.minX + box.getLengthX() * 0.6,
                box.minY + box.getLengthY() * 1.0 * govnoFactor,
                box.minZ + box.getLengthZ() * 0.6
        );

        Vec3 govnoHitVec = RotationUtils.getBestHitVec(box);
        Rotation nearest = RotationUtils.getRotationToPoint(govnoHitVec);

        final Delta originalDelta = RotationUtils.getDelta(startsFrom, nearest);
        Delta delta = originalDelta.copy();

        delta.setYaw(MathHelper.clamp(delta.getYaw(), -simpleYawSpeed, simpleYawSpeed));
        delta.setPitch(MathHelper.clamp(delta.getPitch() , -simplePitchSpeed, simplePitchSpeed));

        delta.setYaw(delta.getYaw() * 0.5f);
        delta.setPitch(delta.getPitch() * 0.5f);

        delta = RotationUtils.fixDelta(delta);

        return new Rotation(
                startsFrom.getYaw() + delta.getYaw(),
                MathHelper.clamp(startsFrom.getPitch() + delta.getPitch(), -90, 90)
        );
    }
}
