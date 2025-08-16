package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;

public class Animation {

    public float x, endX;
    final StopWatch stopWatch;

    public Animation() {
        endX = x = 0;
        stopWatch = new StopWatch();
    }

    public Animation(float x, float endX) {
        this.x = x;
        this.endX = endX;
        stopWatch = new StopWatch();
    }

    public void translateEndPos(float endX) {
        this.endX += endX;
    }

    public void translatePos(float x) {
        this.x += x;
    }

    public void setEndPos(float endX) {
        this.endX = endX;
    }

    public void setPos(float x) {
        this.x = x;
    }

    public void update(float smooth) {
        smooth /= 1000f;
        smooth *= stopWatch.reachedMS();

        float delta = getDelta();
        x += delta * Math.min(smooth, 1);

        stopWatch.reset();
    }

    public float getDelta() {
        return endX - x;
    }

    public void reset() {
        stopWatch.reset();
    }
}
