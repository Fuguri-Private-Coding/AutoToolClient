package fuguriprivatecoding.autotool.utils.math;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomUtils {

    Random random = new Random();

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
}
