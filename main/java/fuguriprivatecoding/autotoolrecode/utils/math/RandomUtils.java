package fuguriprivatecoding.autotoolrecode.utils.math;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomUtils {

    public Random random = new Random();

    /**
     * Возращает рандомное значение от {@code min} до {@code max}
     *
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public int nextInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return Math.round(random.nextFloat(min, max));
    }

    /**
     * Возращает рандомное значение от {@code min} до {@code max}
     *
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public float nextFloat(float min, float max) {
        if (min >= max) {
            return min;
        }
        return random.nextFloat(min, max);
    }

    /**
     * Возращает рандомное значение от {@code min} до {@code max}
     *
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public double nextDouble(double min, double max) {
        if (min >= max) {
            return min;
        }
        return random.nextDouble(min, max);
    }

    /**
     * Возращает рандомное значение от {@code min} до {@code max}
     *
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public long nextLong(long min, long max) {
        if (min >= max) {
            return min;
        }
        return random.nextLong(min, max);
    }

    public static double nextGaussianInRange(double min, double max, double mean, double stdDev) {
        if (min >= max) {
            min = max;
        }
        if (stdDev < 0) {
            stdDev = 0;
        }

        double value = random.nextGaussian() * stdDev + mean;

        if (value < min) {
            value = min + (min - value);
            if (value > max) {
                value = min + (value - min) % (max - min);
            }
        } else if (value > max) {
            value = max - (value - max);
            if (value < min) {
                value = max - (max - value) % (max - min);
            }
        }

        return value;
    }
}
