package fuguriprivatecoding.autotoolrecode.utils.math;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtils implements Imports {

    public static double round(double value, double step) {
        return Math.round(value / step) * step;
    }
    public static double rounds(double number, double step) {
        BigDecimal bdNumber = BigDecimal.valueOf(number);
        BigDecimal bdStep = BigDecimal.valueOf(step);
        BigDecimal divided = bdNumber.divide(bdStep, 0, RoundingMode.HALF_UP);
        return divided.multiply(bdStep).doubleValue();
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = x1 - x2;
        double d1 = y1 - y2;
        double d2 = z1 - z2;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    public static float interpolate(float current, float target) {
        return interpolate(current, target, mc.timer.renderPartialTicks);
    }

    public static float interpolate(float current, float target, float multiple) {
        if (multiple == mc.timer.renderPartialTicks) {
            return current + (target - current) * multiple;
        }

        return current;
    }

    public static double interpolate(double current, double target) {
        return interpolate(current, target, mc.timer.renderPartialTicks);
    }

    public static double interpolate(double current, double target, float multiple) {
        return interpolate((float) current, (float) target, multiple);
    }

    public static Vec3 interpolate(Vec3 current, Vec3 target) {
        return interpolate(current, target, mc.timer.renderPartialTicks);
    }

    public static Vec3 interpolate(Vec3 current, Vec3 target, float multiple) {
        if (multiple == mc.timer.renderPartialTicks) {
            return new Vec3(
                    interpolate(current.xCoord, target.xCoord, multiple),
                    interpolate(current.yCoord, target.yCoord, multiple),
                    interpolate(current.zCoord, target.zCoord, multiple));
        }

        return current;
    }

    public static AxisAlignedBB interpolate(AxisAlignedBB current, AxisAlignedBB target) {
        return interpolate(current, target, mc.timer.renderPartialTicks);
    }

    public static AxisAlignedBB interpolate(AxisAlignedBB current, AxisAlignedBB target, float multiple) {
        if (multiple == mc.timer.renderPartialTicks) {
            return new AxisAlignedBB(
                    interpolate(current.minX, target.minX, multiple),
                    interpolate(current.minY, target.minY, multiple),
                    interpolate(current.minZ, target.minZ, multiple),
                    interpolate(current.maxX, target.maxX, multiple),
                    interpolate(current.maxY, target.maxY, multiple),
                    interpolate(current.maxZ, target.maxZ, multiple)
            );
        }

        return current;
    }

    private static final Random random = new Random();

    public static int rs() {
        return random.nextBoolean() ? 1 : -1;
    }

}
