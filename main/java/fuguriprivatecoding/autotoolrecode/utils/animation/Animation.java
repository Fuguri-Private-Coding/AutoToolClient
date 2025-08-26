package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;

public class Animation {

    public float value, endValue;
    final StopWatch stopWatch;

    public Animation() {
        endValue = value = 0;
        stopWatch = new StopWatch();
    }

    public Animation(float value, float endX) {
        this.value = value;
        this.endValue = endX;
        stopWatch = new StopWatch();
    }

    public void translateEndPos(float endValue) {
        this.endValue += endValue;
    }

    public void translatePos(float value) {
        this.value += value;
    }

    public void setEndPos(float endValue) {
        this.endValue = endValue;
    }

    public void setPos(float x) {
        this.value = x;
    }

    public void update(float smooth) {
        smooth /= 1000f;
        smooth *= stopWatch.reachedMS();

        float delta = getDelta();
        value += delta * Math.min(smooth, 1);

        stopWatch.reset();
    }

    public float getDelta() {
        return endValue - value;
    }

    public void reset() {
        stopWatch.reset();
    }
}
