package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import lombok.Setter;

public class Animation {

    @Setter public float value;
    @Setter public float endValue;

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

    public void translateEndValue(float endValue) {
        this.endValue += endValue;
    }

    public void translateValue(float value) {
        this.value += value;
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
