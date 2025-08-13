package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;

/**
 * Аниматор для трансформаций (позиция, масштаб, прозрачность).
 */
public class TransformAnimator {
    private ValueAnimator xAnimator;
    private ValueAnimator yAnimator;
    private ValueAnimator scaleAnimator;
    private ValueAnimator alphaAnimator;

    public TransformAnimator() {
        // Можно инициализировать с дефолтными значениями
    }

    /**
     * Запускает анимацию перемещения.
     */
    public void animatePosition(double startX, double startY, double endX, double endY, long durationMs, Easing easing) {
        this.xAnimator = new ValueAnimator(startX, endX, durationMs, easing);
        this.yAnimator = new ValueAnimator(startY, endY, durationMs, easing);
    }

    /**
     * Запускает анимацию масштабирования.
     */
    public void animateScale(double startScale, double endScale, long durationMs, Easing easing) {
        this.scaleAnimator = new ValueAnimator(startScale, endScale, durationMs, easing);
    }

    /**
     * Запускает анимацию прозрачности.
     */
    public void animateAlpha(double startAlpha, double endAlpha, long durationMs, Easing easing) {
        this.alphaAnimator = new ValueAnimator(startAlpha, endAlpha, durationMs, easing);
    }

    /**
     * Возвращает текущую позицию X.
     */
    public double getCurrentX() {
        return xAnimator != null ? xAnimator.getCurrentValue() : 0;
    }

    /**
     * Возвращает текущую позицию Y.
     */
    public double getCurrentY() {
        return yAnimator != null ? yAnimator.getCurrentValue() : 0;
    }

    /**
     * Возвращает текущий масштаб.
     */
    public double getCurrentScale() {
        return scaleAnimator != null ? scaleAnimator.getCurrentValue() : 1;
    }

    /**
     * Возвращает текущую прозрачность (0..1).
     */
    public double getCurrentAlpha() {
        return alphaAnimator != null ? alphaAnimator.getCurrentValue() : 1;
    }

    /**
     * Проверяет, завершена ли анимация.
     */
    public boolean isFinished() {
        boolean xFinished = xAnimator == null || xAnimator.isFinished();
        boolean yFinished = yAnimator == null || yAnimator.isFinished();
        boolean scaleFinished = scaleAnimator == null || scaleAnimator.isFinished();
        boolean alphaFinished = alphaAnimator == null || alphaAnimator.isFinished();
        return xFinished && yFinished && scaleFinished && alphaFinished;
    }
}