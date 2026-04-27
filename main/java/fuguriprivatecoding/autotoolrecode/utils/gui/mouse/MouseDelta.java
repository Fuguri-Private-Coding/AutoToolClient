package fuguriprivatecoding.autotoolrecode.utils.gui.mouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MouseDelta {
    int deltaX, deltaY;

    public MouseDelta divine(float smooth) {
        this.deltaX /= (int) smooth;
        this.deltaY /= (int) smooth;
        return this;
    }

    public MouseDelta limit(int speed) {
        this.deltaX = Math.clamp(deltaX, -speed, speed);
        this.deltaY = Math.clamp(deltaY, -speed, speed);
        return this;
    }

    public double length() {
        return Math.hypot(deltaX, deltaY);
    }
}