package fuguriprivatecoding.autotoolrecode.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

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
}
