package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

public class TestRotation extends KillAuraRotation {

    @Override
    public Rotation compute(Rotation startsFrom, EntityLivingBase target, final float simpleYawSpeed, final float simplePitchSpeed) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * 0.4,
                box.minY + box.getLengthY() * 0.5,
                box.minZ + box.getLengthZ() * 0.4,
                box.minX + box.getLengthX() * 0.6,
                box.minY + box.getLengthY() * 0.8,
                box.minZ + box.getLengthZ() * 0.6
        );

        Rotation nearest = RotationUtils.getNearestRotation(startsFrom, box);
        if (nearest == null) { return startsFrom.copy(); }
        return startsFrom.add(RotationUtils.getDelta(startsFrom, nearest).divine(2f, 2f)).fix();
    }

}
