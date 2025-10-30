package fuguriprivatecoding.autotoolrecode.utils.rotation;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

public class RotUtils implements Imports {
	public static Vec3 getBestHitVec(Entity entity) {
		return getBestHitVec(getEntityExpandedBB(entity));
	}

	public static Rot getBestRotation(Entity entity) {
		return getRotationToPoint(getBestHitVec(entity));
	}

	public static Vec3 getBestHitVec(AxisAlignedBB bb) {
		Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
		return bb.clampVecToInside(eyes);
	}

    public static Vec3 getBestHitVec(AxisAlignedBB bb, Vec3 eye) {
        return bb.clampVecToInside(eye);
    }

    public static Rot calculate(final Vector3d from, final Vector3d to) {
        final Vector3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.x, diff.z);
        final float yaw = (float) (MathHelper.atan2(diff.z, diff.x) * 180 / PI) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y, distance) * 180 / PI));
        return new Rot(yaw, pitch);
    }

    public Rot calculate(final Vec3 to, final EnumFacing enumFacing) {
        return calculate(new Vector3d(to.xCoord, to.yCoord, to.zCoord), enumFacing);
    }

    public static Rot calculate(final Vector3d to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), to);
    }

    public static Rot calculate(final Vector3d position, final EnumFacing enumFacing) {
        double x = position.x + 0.5D;
        double y = position.y + 0.5D;
        double z = position.z + 0.5D;

        x += (double) enumFacing.getDirectionVec().getX() * 0.5D;
        y += (double) enumFacing.getDirectionVec().getY() * 0.5D;
        z += (double) enumFacing.getDirectionVec().getZ() * 0.5D;
        return calculate(new Vector3d(x, y, z));
    }

	public static float[] faceTrajectory(Entity target, boolean predict, float predictSize, float gravity, float velocity) {
		double posX = target.posX + (predict ? (target.posX - target.prevPosX) * predictSize : 0.0) - (mc.thePlayer.posX + (predict ? mc.thePlayer.posX - mc.thePlayer.prevPosX : 0.0));
		double posY = target.getEntityBoundingBox().minY + (predict ? (target.getEntityBoundingBox().minY - target.prevPosY) * predictSize : 0.0) + target.getEyeHeight() - 0.15 - (mc.thePlayer.getEntityBoundingBox().minY + (predict ? mc.thePlayer.posY - mc.thePlayer.prevPosY : 0.0)) - mc.thePlayer.getEyeHeight();
		double posZ = target.posZ + (predict ? (target.posZ - target.prevPosZ) * predictSize : 0.0) - (mc.thePlayer.posZ + (predict ? mc.thePlayer.posZ - mc.thePlayer.prevPosZ : 0.0));
		double posSqrt = sqrt(posX * posX + posZ * posZ);

		velocity = min((velocity * velocity + velocity * 2) / 3, 1f);

		float gravityModifier = 0.12f * gravity;

		return new float[]{
				(float) toDegrees(atan2(posZ, posX)) - 90f,
				(float) -toDegrees(atan((velocity * velocity - sqrt(
						velocity * velocity * velocity * velocity - gravityModifier * (gravityModifier * posSqrt * posSqrt + 2 * posY * velocity * velocity)
				)) / (gravityModifier * posSqrt)))
		};
	}

	public static Rot getRotationToBlock(BlockPos blockPos, EnumFacing direction) {
        double centerX = blockPos.getX() + 0.5 + direction.getFrontOffsetX() * 0.5;
        double centerY = blockPos.getY() + 0.5 + direction.getFrontOffsetY() * 0.5;
        double centerZ = blockPos.getZ() + 0.5 + direction.getFrontOffsetZ() * 0.5;

		double playerX = mc.thePlayer.posX;
		double playerY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
		double playerZ = mc.thePlayer.posZ;

		double deltaX = centerX - playerX;
		double deltaY = centerY - playerY;
		double deltaZ = centerZ - playerZ;

		double distanceXZ = sqrt(deltaX * deltaX + deltaZ * deltaZ);
		float yaw = (float) (toDegrees(atan2(deltaZ, deltaX)) - 90.0F);
		float pitch = (float) -toDegrees(atan2(deltaY, distanceXZ));

		return new Rot(yaw, pitch);
	}

	public static Rot getBestRotation(AxisAlignedBB bb) {
		return getRotationToPoint(getBestHitVec(bb));
	}

	public static Vec3 getBestHitVec(Vec3 from, AxisAlignedBB bb) {
		return bb.clampVecToInside(from);
	}

	public static Rot getBestRotation(Vec3 from, AxisAlignedBB bb) {
		return getRotationToPoint(getBestHitVec(from, bb));
	}

	public static Delta getDeltaToPoint(Rot startRotation, Vec3 needPoint) {
		Rot endRotation = getRotationToPoint(needPoint);
		return getDelta(startRotation, endRotation);
	}

	public static Rot getRotationToPoint(Vec3 needPoint) {
		return getRotationFromDiff(needPoint.subtract(mc.thePlayer.getPositionEyes(1.0F)));
	}

	public static Rot getRotationFromDiff(Vec3 diff) {
		double distance = sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);
		return new Rot(
				(float) (Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90),
				(float) -Math.toDegrees(Math.atan2(diff.yCoord, distance))
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

	public static Rot getNearestRotations(Rot from, AxisAlignedBB to) {
		List<Rot> possibleRotations = getPossibleRotations(to, true);
		possibleRotations.sort(Comparator.comparingDouble((rotation) -> {
			Delta delta = getDelta(from, rotation);
			return Math.hypot(Math.abs(delta.getYaw()), Math.abs(delta.getPitch()));
		}));
		return possibleRotations.isEmpty() ? null : possibleRotations.stream().findFirst().orElse(null);
	}

	public static List<Rot> getPossibleRotations(AxisAlignedBB box, boolean removeInvisible) {
		List<Rot> rotations = new ArrayList<>();
		double accuracy = 5.0F;
		double stepX = box.getLengthX() / accuracy;
		double stepY = box.getLengthY() / accuracy;
		double stepZ = box.getLengthZ() / accuracy;

		for(double x = box.minX; x <= box.maxX; x += stepX) {
			for(double y = box.minY; y <= box.maxY; y += stepY) {
				for(double z = box.minZ; z <= box.maxZ; z += stepZ) {
					Vec3 vec = new Vec3(x, y, z);
					if (!removeInvisible || mc.thePlayer.canVecBeSeen(vec)) {
						rotations.add(getRotationToPoint(vec));
					}
				}
			}
		}

		return rotations;
	}

	public static float getMouseGCD() {
		return (float) (Math.pow(mc.gameSettings.mouseSensitivity * 0.6 + 0.2, 3) * 1.2);
	}

	public static Delta fixDelta(Rot delta) {
		final float gcd = getMouseGCD();
		return new Delta(
                (float) MathUtils.round(delta.getYaw(), gcd),
                (float) MathUtils.round(delta.getPitch(), gcd)
        );
	}

	public static Rot getNearestRotation(Rot current, AxisAlignedBB box) {
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

		List<Rot> rotations = Arrays.stream(points)
				.map(RotUtils::getRotationToPoint)
				.toList();

		double minYaw = rotations.stream().mapToDouble(Rot::getYaw).min().orElse(0D);
		double maxYaw = rotations.stream().mapToDouble(Rot::getYaw).max().orElse(0D);
		double minPitch = rotations.stream().mapToDouble(Rot::getPitch).min().orElse(0D);
		double maxPitch = rotations.stream().mapToDouble(Rot::getPitch).max().orElse(0D);

		return new Rot(Math.clamp(current.getYaw(), (float) minYaw, (float) maxYaw),
				Math.clamp(current.getPitch(), (float) minPitch, (float) maxPitch));
	}

	public static Delta getDelta(Rot start, Rot end) {
		return new Delta(
				MathHelper.wrapDegree(end.getYaw() - start.getYaw()),
				end.getPitch() - start.getPitch()
		);
	}

	public static void limitDelta(Rot delta, Rot speed) {
		delta.setYaw(Math.clamp(delta.getYaw(), -speed.getYaw(), speed.getYaw()));
		delta.setPitch(Math.clamp(delta.getPitch(), -speed.getPitch(), speed.getPitch()));
	}

	public static Vec3 getVectorForRotation(Rot rotation) {
		float f = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
		float f3 = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
		return new Vec3(f1 * f2, f3, f * f2);
	}
}
