package fuguriprivatecoding.autotoolrecode.utils.player.distance;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class DistanceUtils implements Imports {

    public static double getDistance(Entity entity) {
        return getDistance(mc.thePlayer.getPositionEyes(1.0F), entity);
    }

    public static double getDistance(Vec3 eyes, Entity entity) {
        return eyes.distanceTo(RotUtils.getBestHitVec(entity));
    }

	public static double getDistance(Vec3 pos) {
		return getDistance(mc.thePlayer.getPositionEyes(1.0F), pos);
	}

    public static double getDistance(Vec3 eyes, Vec3 pos) {
        return eyes.distanceTo(pos);
    }

    public static double getDistance(BlockPos pos) {
        return getDistance(mc.thePlayer.getPositionEyes(1.0F), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static double getDistance(Vec3 eyes, BlockPos pos) {
        return getDistance(eyes, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
    }

	public static double getDistance(AxisAlignedBB bb) {
		return getDistance(mc.thePlayer.getPositionEyes(1.0F), bb);
	}

    public static double getDistance(Vec3 eyes, AxisAlignedBB bb) {
        return eyes.distanceTo(RotUtils.getBestHitVec(bb));
    }
}
