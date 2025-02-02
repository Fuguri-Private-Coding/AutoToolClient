package me.hackclient.utils.math;

public class MathUtils {

    public static double round(double value, double step) {
        return Math.round(value / step) * step;
    }
}
