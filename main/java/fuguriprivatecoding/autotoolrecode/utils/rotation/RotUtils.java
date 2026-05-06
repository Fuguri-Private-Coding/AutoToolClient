package fuguriprivatecoding.autotoolrecode.utils.rotation;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class RotUtils implements Imports {
	public static Vec3 getBestHitVec(Entity entity) {
		return getBestHitVec(getEntityExpandedBB(entity));
	}

	public static Vec3 getBestHitVec(AxisAlignedBB bb) {
		return getBestHitVec(bb, mc.thePlayer.getPositionEyes(1.0F));
	}

    public static Vec3 getBestHitVec(AxisAlignedBB bb, Vec3 eye) {
        return bb.clampVecToInside(eye);
    }

    public static AxisAlignedBB getHitBox(EntityLivingBase target, double horizontal, double vertical) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double horizontalPercent = horizontal / 200d;
        double verticalPercent = vertical / 200d;

        Vec3 center = new Vec3(
            (box.maxX + box.minX) / 2,
            (box.maxY + box.minY) / 2,
            (box.maxZ + box.minZ) / 2
        );

        box = new AxisAlignedBB(
            center.xCoord - box.getLengthX() * horizontalPercent,
            center.yCoord - box.getLengthY() * verticalPercent,
            center.zCoord - box.getLengthZ() * horizontalPercent,
            center.xCoord + box.getLengthX() * horizontalPercent,
            center.yCoord + box.getLengthY() * verticalPercent,
            center.zCoord + box.getLengthZ() * horizontalPercent
        );

        return box;
    }

	public static Rot getBestRotation(AxisAlignedBB bb) {
		return getRotationToPoint(getBestHitVec(bb));
	}

	public static Rot getDeltaToPoint(Rot startRotation, Vec3 needPoint) {
		Rot endRotation = getRotationToPoint(needPoint);
		return startRotation.deltaTo(endRotation);
	}

	public static Rot getRotationToPoint(Vec3 needPoint) {
		return getRotationFromDiff(needPoint.subtract(mc.thePlayer.getPositionEyes(1.0F)));
	}

	public static Rot getRotationFromDiff(Vec3 diff) {
		double distance = sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);
		return new Rot(
			MathHelper.wrapDegree((float) (Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90)),
			(float) -Math.toDegrees(Math.atan2(diff.yCoord, distance))
		);
	}

	public static Rot getNearestRotation(Rot current, AxisAlignedBB bb) {
		Vec3[] points = getPoints(bb);

		List<Rot> rotations = Arrays.stream(points).map(RotUtils::getRotationToPoint).toList();

		double minYaw = rotations.stream().mapToDouble(Rot::getYaw).min().orElse(0D);
		double maxYaw = rotations.stream().mapToDouble(Rot::getYaw).max().orElse(0D);
		double minPitch = rotations.stream().mapToDouble(Rot::getPitch).min().orElse(0D);
		double maxPitch = rotations.stream().mapToDouble(Rot::getPitch).max().orElse(0D);

		return new Rot(Math.clamp(MathHelper.wrapDegree(current.getYaw()), (float) minYaw, (float) maxYaw),
			Math.clamp(current.getPitch(), (float) minPitch, (float) maxPitch));
	}

    public static Vec3[] getPoints(AxisAlignedBB bb) {
        return new Vec3[] {
            new Vec3(bb.minX, bb.minY, bb.minZ),
            new Vec3(bb.maxX, bb.minY, bb.minZ),
            new Vec3(bb.maxX, bb.minY, bb.maxZ),
            new Vec3(bb.minX, bb.minY, bb.maxZ),
            new Vec3(bb.minX, bb.maxY, bb.minZ),
            new Vec3(bb.maxX, bb.maxY, bb.minZ),
            new Vec3(bb.maxX, bb.maxY, bb.maxZ),
            new Vec3(bb.minX, bb.maxY, bb.maxZ)
        };
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
		return Math.abs(MathHelper.wrapDegree(yaw - CameraRot.INST.getYaw()));
	}

    public static Rot getPossibleBestRotation(Rot startRot, AxisAlignedBB box) {
        double accuracy = 5.0F;
        double stepX = box.getLengthX() / accuracy;
        double stepY = box.getLengthY() / accuracy;
        double stepZ = box.getLengthZ() / accuracy;
        double nearest = 15.0;

        Vec3 best = null;

        for (double x = box.minX; x <= box.maxX; x += stepX) {
            for (double z = box.minZ; z <= box.maxZ; z += stepZ) {
                for (double y = box.minY; y <= box.maxY; y += stepY) {
                    Vec3 pos = new Vec3(x, y, z);
                    if (mc.thePlayer.canVecBeSeen(pos)) {
                        double distance = DistanceUtils.getDistance(pos);
                        if (distance <= nearest) {
                            nearest = distance;
                            best = pos;
                        }
                    }
                }
            }
        }

        return best != null ? getRotationToPoint(best) : startRot;
    }

	public static float getMouseGCD() {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        return f * f * f * 8.0F * 0.15F;
	}

	public static Vec3 getVectorForRotation(Rot rotation) {
		float f = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
		float f3 = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
		return new Vec3(f1 * f2, f3, f * f2);
	}
}