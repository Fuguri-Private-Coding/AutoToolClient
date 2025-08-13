package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;

/**
 * Утилиты для анимации с использованием easing-функций.
 */
public class AnimationUtils {

    /**
     * Интерполирует значение от start до end с применением easing-функции.
     *
     * @param start начальное значение
     * @param end конечное значение
     * @param progress прогресс анимации (0..1)
     * @param easing функция плавности
     * @return интерполированное значение
     */
    public static double animate(double start, double end, double progress, Easing easing) {
        return start + (end - start) * easing.get(progress);
    }

    /**
     * Аналог для float.
     */
    public static float animate(float start, float end, float progress, Easing easing) {
        return (float) animate((double) start, (double) end, (double) progress, easing);
    }

    /**
     * Аналог для int (округление до ближайшего целого).
     */
    public static int animate(int start, int end, double progress, Easing easing) {
        return (int) Math.round(animate((double) start, (double) end, progress, easing));
    }

    /**
     * Зацикленная анимация (например, для пульсации).
     *
     * @param progress прогресс (0..1)
     * @param easing функция плавности
     * @return значение в диапазоне [0..1]
     */
    public static double pingPong(double progress, Easing easing) {
        return progress < 0.5 
                ? easing.get(progress * 2) 
                : easing.get(1 - (progress - 0.5) * 2);
    }

    /**
     * Анимация с повторением (например, для бесконечной анимации).
     *
     * @param progress прогресс (может быть >1)
     * @param easing функция плавности
     * @return значение в диапазоне [0..1]
     */
    public static double loop(double progress, Easing easing) {
        return easing.get(progress - Math.floor(progress));
    }
}