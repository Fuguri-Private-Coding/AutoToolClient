package fuguriprivatecoding.autotoolrecode.utils.interpolation;

import lombok.AllArgsConstructor;

import java.util.function.Function;

import static java.lang.Math.*;

/**
 * Все анимации были взяты с сайта <a href="https://easings.net/">easings.net</a>
 */
@AllArgsConstructor
public enum Easing {
    LINEAR(x -> x),

    // Sine
    EASE_IN_SINE(x -> 1 - cos(x * PI / 2)),
    EASE_OUT_SINE(x -> sin(x * PI / 2)),
    EASE_IN_OUT_SINE(x -> -(cos(PI * x) - 1) / 2),

    // Quad
    EASE_IN_QUAD(x -> x * x),
    EASE_OUT_QUAD(x -> 1 - (1 - x) * (1 - x)),
    EASE_IN_OUT_QUAD(x -> x < 0.5 ? 2 * x * x : 1 - pow(-2 * x + 2, 2) / 2),

    // Cubic
    EASE_IN_CUBIC(x -> x * x * x),
    EASE_OUT_CUBIC(x -> 1 - pow(1 - x, 3)),
    EASE_IN_OUT_CUBIC(x -> x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2),

    // Quart
    EASE_IN_QUART(x -> x * x * x * x),
    EASE_OUT_QUART(x -> 1 - pow(1 - x, 4)),
    EASE_IN_OUT_QUART(x -> x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2),

    // Quint
    EASE_IN_QUINT(x -> x * x * x * x * x),
    EASE_OUT_QUINT(x -> 1 - pow(1 - x, 5)),
    EASE_IN_OUT_QUINT(x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - pow(-2 * x + 2, 5) / 2),

    // Expo
    EASE_IN_EXPO(x -> x == 0 ? 0 : pow(2, 10 * x - 10)),
    EASE_OUT_EXPO(x -> x == 1 ? 1 : 1 - pow(2, -10 * x)),
    EASE_IN_OUT_EXPO(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? pow(2, 20 * x - 10) / 2 : (2 - pow(2, -20 * x + 10)) / 2),

    // Circ
    EASE_IN_CIRC(x -> 1 - sqrt(1 - pow(x, 2))),
    EASE_OUT_CIRC(x -> sqrt(1 - pow(x - 1, 2))),
    EASE_IN_OUT_CIRC(x -> x < 0.5 ? (1 - sqrt(1 - pow(2 * x, 2))) / 2 : (sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2),

    // Back
    EASE_IN_BACK(x -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }),
    EASE_OUT_BACK(x -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * pow(x - 1, 3) + c1 * pow(x - 1, 2);
    }),
    EASE_IN_OUT_BACK(x -> {
        double c1 = 1.70158;
        double c2 = c1 * 1.525;
        return x < 0.5
                ? (pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2) / 2)
                : (pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }),

    // Elastic
    EASE_IN_ELASTIC(x -> {
        if (x == 0) return 0D;
        if (x == 1) return 1D;
        double c4 = (2 * PI) / 3;
        return -pow(2, 10 * x - 10) * sin((x * 10 - 10.75) * c4);
    }),
    EASE_OUT_ELASTIC(x -> {
        if (x == 0) return 0D;
        if (x == 1) return 1D;
        double c4 = (2 * PI) / 3;
        return pow(2, -10 * x) * sin((x * 10 - 0.75) * c4) + 1;
    }),
    EASE_IN_OUT_ELASTIC(x -> {
        if (x == 0) return 0D;
        if (x == 1) return 1D;
        double c5 = (2 * PI) / 4.5;
        double sin = sin((20 * x - 11.125) * c5);
        return x < 0.5
                ? -(pow(2, 20 * x - 10) * sin) / 2
                : (pow(2, -20 * x + 10) * sin) / 2 + 1;
    }),

    // Bounce
    EASE_OUT_BOUNCE(x -> {
        if (x < 1 / 2.75) return 7.5625 * x * x;
        else if (x < 2 / 2.75) return 7.5625 * (x -= 1.5 / 2.75) * x + 0.75;
        else if (x < 2.5 / 2.75) return 7.5625 * (x -= 2.25 / 2.75) * x + 0.9375;
        else return 7.5625 * (x -= 2.625 / 2.75) * x + 0.984375;
    }),
    EASE_IN_BOUNCE(x -> 1 - EASE_OUT_BOUNCE.function.apply(1 - x)),
    EASE_IN_OUT_BOUNCE(x -> x < 0.5
            ? (1 - Easing.EASE_OUT_BOUNCE.function.apply(1 - 2 * x)) / 2
            : (1 + Easing.EASE_OUT_BOUNCE.function.apply(2 * x - 1)) / 2),

    // Sigmoid (дополнительная функция)
    SIGMOID(x -> 1 / (1 + exp(-x)));

    private final Function<Double, Double> function;

    public double get(double t) {
        return function.apply(t);
    }
}