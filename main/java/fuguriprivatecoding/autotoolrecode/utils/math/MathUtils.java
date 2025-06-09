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
}
