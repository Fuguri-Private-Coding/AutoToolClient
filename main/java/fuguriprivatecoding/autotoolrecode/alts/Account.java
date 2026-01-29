package fuguriprivatecoding.autotoolrecode.alts;

import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.Getter;
import lombok.Setter;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@Getter
@Setter
public class Account {
    String name;
    String refreshToken, uuid;

    boolean deleting;

    AccountType type;

    EasingAnimation hoverAnim = new EasingAnimation();
    EasingAnimation deleteAnim = new EasingAnimation();
    EasingAnimation selectAnim = new EasingAnimation();

    public Account(String name) {
        this.name = name;
        this.type = AccountType.OFFLINE;
    }

    public Account(String name, String refreshToken, String uuid) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.uuid = uuid;
        this.type = AccountType.MICROSOFT;
    }

    public void updateAnimations() {
        deleteAnim.update(4f, Easing.OUT_CUBIC);
        hoverAnim.update(2, Easing.OUT_BACK);
        selectAnim.update(4f,Easing.OUT_CUBIC);
    }

    public boolean isDelete() {
        return isDeleting() && !deleteAnim.isAnimating();
    }

    public boolean isSession() {
        return mc.getSession().getUsername().equals(name);
    }

    public enum AccountType {
        OFFLINE, MICROSOFT, MCTOKEN
    }
}
