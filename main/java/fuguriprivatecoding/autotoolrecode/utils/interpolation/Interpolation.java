package fuguriprivatecoding.autotoolrecode.utils.interpolation;

import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import lombok.Getter;

@Getter
public class Interpolation extends StopWatch {
    private Easing easing;
    private boolean forward = true;
    private long duration;

    public Interpolation(Easing easing, long duration) {
        this.easing = easing;
        this.duration = duration;
    }

    public Interpolation setEasing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public Interpolation setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public Interpolation setForward(boolean forward) {
        this.forward = forward;
        return this;
    }

    public double getRaw() {
        return Math.clamp((double) reachedMS() / (double) duration, 0d, 1d);
    }

    public double get() {
        return easing.get(getRaw());
    }
}
