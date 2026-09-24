package fuguriprivatecoding.autotoolrecode.key;

public interface KeyListener {
    void onTick(boolean pressed);
    boolean shouldListenKey();
    int getKey();
    default void registerToKeyBinds() {
        KeyCaller.getInstance().register(this);
    };
}