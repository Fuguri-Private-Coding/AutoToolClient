package fuguriprivatecoding.autotoolrecode.alt;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    String name;
    String refreshToken, uuid;

    boolean deleting;

    EasingAnimation anim = new EasingAnimation(0);

    public Account(String name) {
        this.name = name;
    }

    public Account(String name, String refreshToken, String uuid) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.uuid = uuid;
    }
}
