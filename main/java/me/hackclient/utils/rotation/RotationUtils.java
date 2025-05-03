package me.hackclient.utils.rotation;

import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.math.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.List;

public class RotationUtils implements Imports {
	public static Vec3 getBestHitVec(Entity entity) {
		return getBestHitVec(getEntityExpandedBB(entity));
	}

	public static Rotation getBestRotation(Entity entity) {
		return getRotationToPoint(getBestHitVec(entity));
	}

	public static Vec3 getBestHitVec(AxisAlignedBB bb) {
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return bb.clampVecToInside(eyes);
	}

	public static Rotation getBestRotation(AxisAlignedBB bb) {
		return getRotationToPoint(getBestHitVec(bb));
	}

	public static Vec3 getBestHitVec(Vec3 from, AxisAlignedBB bb) {
		return bb.clampVecToInside(from);
	}

	public static Rotation getBestRotation(Vec3 from, AxisAlignedBB bb) {
		return getRotationToPoint(getBestHitVec(from, bb));
	}

	public static Delta getDeltaToPoint(Rotation startRotation, Vec3 needPoint) {
		Rotation endRotation = getRotationToPoint(needPoint);
		return getDelta(startRotation, endRotation);
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
		return (float) (Math.pow(mc.gameSettings.mouseSensitivity * 0.6 + 0.2, 3) * 1.2);
	}

	public static Delta fixDelta(Rotation delta) {
		final float gcd = getMouseGCD();
		return new Delta(
                (float) MathUtils.round(delta.getYaw(), gcd),
                (float) MathUtils.round(delta.getPitch(), gcd)
        );
	}

	public static Rotation getNearestRotation(Rotation current, AxisAlignedBB box) {
		Vec3[] points = {
				new Vec3(box.minX, box.minY, box.minZ),
				new Vec3(box.maxX, box.minY, box.minZ),
				new Vec3(box.minX, box.maxY, box.minZ),
				new Vec3(box.minX, box.minY, box.maxZ),
				new Vec3(box.maxX, box.maxY, box.minZ),
				new Vec3(box.maxX, box.minY, box.maxZ),
				new Vec3(box.minX, box.maxY, box.maxZ),
				new Vec3(box.maxX, box.maxY, box.maxZ)
		};

		List<Rotation> rotations = Arrays.stream(points)
				.map(RotationUtils::getRotationToPoint)
				.toList();

		double minYaw = rotations.stream().mapToDouble(Rotation::getYaw).min().orElse(0D);
		double maxYaw = rotations.stream().mapToDouble(Rotation::getYaw).max().orElse(0D);
		double minPitch = rotations.stream().mapToDouble(Rotation::getPitch).min().orElse(0D);
		double maxPitch = rotations.stream().mapToDouble(Rotation::getPitch).max().orElse(0D);

		return new Rotation(Math.clamp(current.getYaw(), (float) minYaw, (float) maxYaw),
				Math.clamp(current.getPitch(), (float) minPitch, (float) maxPitch));
	}

	public static Delta getDelta(Rotation start, Rotation end) {
		return new Delta(
				MathHelper.wrapDegree(end.getYaw() - start.getYaw()),
				end.getPitch() - start.getPitch()
		);
	}

	public static void limitDelta(Rotation delta, Rotation speed) {
		delta.setYaw(Math.clamp(delta.getYaw(), -speed.getYaw(), speed.getYaw()));
		delta.setPitch(Math.clamp(delta.getPitch(), -speed.getPitch(), speed.getPitch()));
	}

	public static Vec3 getVectorForRotation(Rotation rotation) {
		float f = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
		float f3 = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
		return new Vec3(f1 * f2, f3, f * f2);
	}
}
