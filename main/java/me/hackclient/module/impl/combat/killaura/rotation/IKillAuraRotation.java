package me.hackclient.module.impl.combat.killaura.rotation;

import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public interface IKillAuraRotation {
	Rotation compute(Rotation current, EntityLivingBase target, float yawStep, float pitchStep);
	void setRandomOffset(Vec3 offset);
	Vec3 getRandomOffset();
}
