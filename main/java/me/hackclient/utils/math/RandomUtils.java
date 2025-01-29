package me.hackclient.utils.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomUtils {

    /**
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public int nextInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextInt(min, max);
    }

    /**
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public float nextFloat(float min, float max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextFloat(min, max);
    }

    /**
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public double nextDouble(double min, double max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextDouble(min, max);
    }

    /**
     @param min Минимум рандомного значения
     @param max Максимум рандомного значения
     */
    public long nextLong(long min, long max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextLong(min, max);
    }
}
