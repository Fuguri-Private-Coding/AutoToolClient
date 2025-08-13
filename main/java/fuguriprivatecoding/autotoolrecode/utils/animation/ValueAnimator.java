package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;

/**
 * Аниматор для плавного изменения значения во времени.
 */
public class ValueAnimator {
    private double startValue;
    private double endValue;
    private long durationMs;
    private long startTime;
    private Easing easing;

    public ValueAnimator(double startValue, double endValue, long durationMs, Easing easing) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.durationMs = durationMs;
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Возвращает текущее анимированное значение.
     */
    public double getCurrentValue() {
        long elapsed = System.currentTimeMillis() - startTime;
        double progress = Math.min((double) elapsed / durationMs, 1.0);
        return AnimationUtils.animate(startValue, endValue, progress, easing);
    }

    /**
     * Проверяет, завершена ли анимация.
     */
    public boolean isFinished() {
        return System.currentTimeMillis() - startTime >= durationMs;
    }

    /**
     * Перезапускает анимацию.
     */
    public void restart() {
        this.startTime = System.currentTimeMillis();
    }
}