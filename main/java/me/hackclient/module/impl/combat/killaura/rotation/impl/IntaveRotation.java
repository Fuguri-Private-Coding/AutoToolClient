package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.IKillAuraRotation;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class IntaveRotation implements IKillAuraRotation {

	boolean startSlowRotation;

	@Override
	public Rotation compute(Rotation current, EntityLivingBase target, float yawStep, float pitchStep) {
		AxisAlignedBB box = target.getEntityBoundingBox();

		box = new AxisAlignedBB(
				box.minX + box.getLengthX() * 0.4,
				box.minY + box.getLengthY() * 0.5,
				box.minZ + box.getLengthZ() * 0.4,
				box.minX + box.getLengthX() * 0.6,
				box.minY + box.getLengthY() * 0.85,
				box.minZ + box.getLengthZ() * 0.6
		);

		Rotation nearest = RotationUtils.getNearestRotation(current, box);
		if (nearest == null) {
			return new Rotation(current.getYaw(), current.getPitch());
		}

		Delta delta = RotationUtils.getDelta(current, nearest);

		Delta delta1 = delta.copy();

		delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawStep, yawStep));
		delta.setPitch(MathHelper.clamp(delta.getPitch() , -pitchStep, pitchStep));

		delta.setYaw(delta.getYaw() * 0.5f);
		delta.setPitch(delta.getPitch() * 0.5f);

		if (startSlowRotation) {
			delta.setYaw(delta.getYaw() * 0.2f);
			delta.setPitch(delta.getPitch() * 0.2f);
		}

		delta = RotationUtils.fixDelta(delta);

		if (delta1.hypot() < 1) startSlowRotation = true;

		return new Rotation(
				current.getYaw() + delta.getYaw(),
				MathHelper.clamp(current.getPitch() + delta.getPitch(), -90, 90)
		);
	}

	@Override
	public void setRandomOffset(Vec3 offset) {
		return;
	}

	@Override
	public Vec3 getRandomOffset() {
		return null;
	}

}