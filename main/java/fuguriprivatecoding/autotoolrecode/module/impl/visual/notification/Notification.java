package fuguriprivatecoding.autotoolrecode.module.impl.visual.notification;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.Getter;

@Getter
public class Notification {
    String text;
    boolean toggled;

    long lastTime;
    long lifeTime;

    EasingAnimation openAnim = new EasingAnimation();

    public Notification(String text, boolean toggled, long lastTime, long lifeTime) {
        this.text = text;
        this.toggled = toggled;
        this.lastTime = lastTime;
        this.lifeTime = lifeTime;
        openAnim.setEnd(1);
    }

    public boolean isDelete() {
        return System.currentTimeMillis() - lastTime >= lifeTime;
    }
}
