package me.hackclient.utils.distance;

import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class DistanceUtils implements InstanceAccess {
	public static double getDistanceToEntity(Entity entity) {
		Vec3 nearestPoint = RotationUtils.getBestHitVec(entity);
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return eyes.distanceTo(nearestPoint);
	}
}
