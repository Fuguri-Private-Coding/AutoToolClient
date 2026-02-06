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
        return new MouseDelta((int) (deltaX / smooth), (int) (deltaY / smooth));
    }

    public MouseDelta limit(int speed) {
        return new MouseDelta(Math.clamp(deltaX, -speed, speed), Math.clamp(deltaY, -speed, speed));
    }

    public double hypot() {
        return Math.hypot(deltaX, deltaY);
    }
}