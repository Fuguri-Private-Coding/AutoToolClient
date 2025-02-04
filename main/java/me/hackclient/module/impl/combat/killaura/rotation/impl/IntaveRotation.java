package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class IntaveRotation extends KillAuraRotation {

    //boolean startSlow;

    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, final float simpleYawSpeed, final float simplePitchSpeed) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * 0.4,
                box.minY + box.getLengthY() * 0.0,
                box.minZ + box.getLengthZ() * 0.4,
                box.minX + box.getLengthX() * 0.6,
                box.minY + box.getLengthY() * 0.8,
                box.minZ + box.getLengthZ() * 0.6
        );

        Rotation nearest = RotationUtils.getNearestRotation(startsFrom, box);
        if (nearest == null) { return startsFrom.copy(); }

        final Delta originalDelta = RotationUtils.getDelta(startsFrom, nearest);
        Delta delta = originalDelta.copy();

        delta.setYaw(MathHelper.clamp(delta.getYaw(), -simpleYawSpeed, simpleYawSpeed));
        delta.setPitch(MathHelper.clamp(delta.getPitch() , -simplePitchSpeed, simplePitchSpeed));

        delta.setYaw(delta.getYaw() * 0.5f);
        delta.setPitch(delta.getPitch() * 0.5f);

//        if (startSlow) {
//            delta.setYaw(delta.getYaw() * 0.2F);
//            delta.setPitch(delta.getPitch() * 0.2F);
//            startSlow = false;
//        }
//
//        if (originalDelta.hypot() < 1) { startSlow = true; }

        delta = RotationUtils.fixDelta(delta);

        return new Rotation(
                startsFrom.getYaw() + delta.getYaw(),
                MathHelper.clamp(startsFrom.getPitch() + delta.getPitch(), -90, 90)
        );
    }

}
