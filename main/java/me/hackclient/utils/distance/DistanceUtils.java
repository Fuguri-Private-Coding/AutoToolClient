package me.hackclient.utils.distance;

import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.rotation.RotUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class DistanceUtils implements Imports {

	/**
	 * @param entity Ентити до хитбокса которого будет вычеслятся растояние.
	 */
	public static double getDistance(Entity entity) {
		Vec3 nearestPoint = RotUtils.getBestHitVec(entity);
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return eyes.distanceTo(nearestPoint);
	}

	/**
	 * @param pos трех-мерный вектор до которого будет вычеслятся растояние.
	 */
	public static double getDistance(Vec3 pos) {
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return eyes.distanceTo(pos);
	}


	public static double getDistance(AxisAlignedBB bb) {
		return mc.thePlayer.getPositionEyes(1.0f).distanceTo(RotUtils.getBestHitVec(bb));
	}
}
