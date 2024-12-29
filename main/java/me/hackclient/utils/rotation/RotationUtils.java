package me.hackclient.utils.rotation;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RotationUtils implements InstanceAccess {
	public static Vec3 getBestHitVec(Entity entity) {
		return getBestHitVec(getEntityExpandedBB(entity));
	}

	public static Vec3 getBestHitVec(AxisAlignedBB bb) {
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return bb.clampVecToInside(eyes);
	}

	public static Delta getDeltaToPoint(Rotation startRotation, Vec3 needPoint) {
		Rotation endRotation = getRotationToPoint(needPoint);
		return (Delta) endRotation.subtract(startRotation);
	}

	public static Rotation getRotationToPoint(Vec3 needPoint) {
		Vec3 delta = needPoint.subtract(mc.thePlayer.getPositionEyes(1.0F));
		double distance = mc.thePlayer.getPositionEyes(1.0F).distanceTo(needPoint);
		return new Rotation(
				(float) (Math.toDegrees(Math.atan2(delta.zCoord, delta.xCoord)) - 90),
				(float) -Math.toDegrees(Math.atan2(delta.yCoord, distance))
		);
	}

	public static AxisAlignedBB getEntityExpandedBB(Entity entity) {
		return entity.getEntityBoundingBox().expand(
				entity.getCollisionBorderSize(),
				entity.getCollisionBorderSize(),
				entity.getCollisionBorderSize()
		);
	}

	public static float getFovToEntity(Entity entity) {
		Vec3 delta = entity.getPositionVector().subtract(mc.thePlayer.getPositionEyes(1.0F));
		float yaw = (float) (Math.toDegrees(MathHelper.atan2(delta.zCoord, delta.xCoord))) - 90;
		return Math.abs(MathHelper.wrapDegree(yaw - mc.thePlayer.rotationYaw));
	}

	public static float getMouseGCD() {
		return (float) (Math.pow(mc.gameSettings.mouseSensitivity  * 0.6F + 0.2, 3) * 1.2F);
	}

	public static Delta fixDelta(Rotation delta) {
		final float gcd = getMouseGCD();
		return new Delta(
				delta.getYaw() - (delta.getYaw() % gcd),
				delta.getPitch() - (delta.getPitch() % gcd)
		);
	}

	public static Rotation getNearestRotation(Rotation from, AxisAlignedBB to) {
		List<Rotation> possibleRotations = getPossibleRotations(to, true);
		System.out.println("starting sort");
		possibleRotations.sort(Comparator.comparingDouble(
				rotation -> {
					Delta delta = getDelta(from, rotation);
					return Math.hypot(Math.abs(delta.getYaw()), Math.abs(delta.getPitch()));
				}
		));
		if (possibleRotations.isEmpty()) return null;
		return possibleRotations.stream().findFirst().orElse(null);
	}

	public static List<Rotation> getPossibleRotations(AxisAlignedBB box, boolean removeInvisible) {
		List<Rotation> rotations = new ArrayList<>();

		double accuracy = 5;
		double stepX = box.getLengthX() / accuracy;
		double stepY = box.getLengthY() / accuracy;
		double stepZ = box.getLengthZ() / accuracy;


		for (double x = box.minX; x <= box.maxX; x += stepX) {
			for (double y = box.minY; y <= box.maxY; y += stepY) {
				for (double z = box.minZ; z <= box.maxZ; z += stepZ) {
					Vec3 vec = new Vec3(x, y, z);
					if (!removeInvisible || mc.thePlayer.canVecBeSeen(vec)) {
						rotations.add(RotationUtils.getRotationToPoint(vec));
					}
				}
			}
		}

		return rotations;
	}

	public static Delta getDelta(Rotation start, Rotation end) {
		return new Delta(
				MathHelper.wrapDegree(end.getYaw() - start.getYaw()),
				end.getPitch() - start.getPitch()
		);
	}
}
