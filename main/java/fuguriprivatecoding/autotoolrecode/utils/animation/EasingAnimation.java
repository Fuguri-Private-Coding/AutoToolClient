package fuguriprivatecoding.autotoolrecode.utils.animation;

import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EasingAnimation {
    private float value;
    private float start;
    private float end;
    private float progress;

    public EasingAnimation() {
        this(0f);
    }
    
    public EasingAnimation(float initialValue) {
        this.value = initialValue;
        this.start = initialValue;
        this.end = initialValue;
        this.progress = 1f;
    }

    public void reset() {
        this.value = end;
        this.start = end;
        this.progress = 1f;
    }
    
    public void update(float speed, Easing easingFunction) {
        if (progress < 1f) {
            progress += speed * DeltaTracker.getMilli() / 1000;
            progress = Math.min(progress, 1f);
            
            float easedProgress = (float) easingFunction.get(progress);
            value = start + (end - start) * easedProgress;
        } else {
            value = end;
        }
    }

    public void setEnd(boolean hovered) {
        setEnd(hovered ? 1 : 0);
    }
    
    public void setEnd(float endValue) {
        if (this.end != endValue) {
            this.start = this.value;
            this.end = endValue;
            this.progress = 0f;
        }
    }

    public boolean isAnimating() {
        return progress < 1f;
    }
    
    public void setValue(float value) {
        this.value = value;
        this.start = value;
        this.end = value;
        this.progress = 1f;
    }
}