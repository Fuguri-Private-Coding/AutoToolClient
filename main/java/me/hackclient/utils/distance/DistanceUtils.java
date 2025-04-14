package me.hackclient.utils.distance;

import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.io.IOException;

public class DistanceUtils implements InstanceAccess {

	/**
	 * @param entity Ентити до хитбокса которого будет вычеслятся растояние.
	 */
	public static double getDistanceToEntity(Entity entity) {
		Vec3 nearestPoint = RotationUtils.getBestHitVec(entity);
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return eyes.distanceTo(nearestPoint);
	}

	/**
	 * @param pos трех-мерный вектор до которого будет вычеслятся растояние.
	 */
	public static double getDistanceToVec(Vec3 pos) {
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return eyes.distanceTo(pos);
	}


	public static double getDistanceToHitBox(AxisAlignedBB bb) {
		return mc.thePlayer.getPositionEyes(1.0f).distanceTo(RotationUtils.getBestHitVec(bb));
	}
}
