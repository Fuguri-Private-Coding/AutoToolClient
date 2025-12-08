package fuguriprivatecoding.autotoolrecode.module.impl.visual.notification;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class Notification {
    String name;
    boolean toggled;

    EasingAnimation openAnim = new EasingAnimation();

    long lastTime;
    long time;

    public Notification(String name, boolean toggled, long lastTime, long time) {
        this.name = name;
        this.toggled = toggled;
        this.lastTime = lastTime;
        this.time = time;
    }

    public boolean isDelete() {
        return System.currentTimeMillis() - lastTime >= time;
    }
}
