package me.hackclient.module.impl.combat.killaura.rotation.impl;

import me.hackclient.module.impl.combat.killaura.rotation.IKillAuraRotation;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class LinearRotation implements IKillAuraRotation {

	private Vec3 randomOffset = new Vec3(0, 0, 0);

	@Override
	public Rotation compute(Rotation current, EntityLivingBase target, float yawStep, float pitchStep) {
		return RotationUtils.getRotationToPoint(target.getPositionEyes(1.0F));
	}

	@Override
	public void setRandomOffset(Vec3 offset) {
		randomOffset = offset;
	}

	@Override
	public Vec3 getRandomOffset() {
		return randomOffset;
	}
}
