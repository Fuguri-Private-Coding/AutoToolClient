package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import org.lwjgl.util.vector.Vector2f;

public class Animation2D {
    public float x, y, endX, endY;
    final StopWatch stopWatch;

    public Animation2D() {
        endX = endY = x = y = 0;
        stopWatch = new StopWatch();
    }

    public Animation2D(float x, float y, float endX, float endY) {
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        stopWatch = new StopWatch();
    }

    public void translateEndPos(float endX, float endY) {
        this.endX += endX;
        this.endY += endY;
    }

    public void translatePos(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void setEndPos(float endX, float endY) {
        this.endX = endX;
        this.endY = endY;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float smooth) {
        smooth /= 1000f;
        smooth *= stopWatch.reachedMS();

        Vector2f delta = getDelta();
        x += delta.x * Math.min(smooth, 1);
        y += delta.y * Math.min(smooth, 1);

        stopWatch.reset();
    }

    public Vector2f getDelta() {
        return new Vector2f(
                (float) (endX - x),
                (float) (endY - y)
        );
    }

    public void reset() {
        stopWatch.reset();
    }
}
