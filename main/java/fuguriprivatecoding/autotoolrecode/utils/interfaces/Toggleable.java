package fuguriprivatecoding.autotoolrecode.utils.interfaces;

// created by dicves_recode on 11.02.2026
public interface Toggleable {
    boolean isToggled();
    void setToggled(boolean toggled);

    default void toggle() {
        setToggled(!isToggled());

        if (isToggled()) {
            onEnable();
        } else {
            onDisable();
        }
    }

    default void onEnable() {}
    default void onDisable() {}
}
