package me.hackclient.utils.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomUtils {

    public int nextInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextInt(min, max);
    }

    public float nextFloat(float min, float max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextFloat(min, max);
    }

    public double nextDouble(double min, double max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextDouble(min, max);
    }

    public long nextLong(long min, long max) {
        if (min >= max) {
            return min;
        }
        return org.apache.commons.lang3.RandomUtils.nextLong(min, max);
    }
}
