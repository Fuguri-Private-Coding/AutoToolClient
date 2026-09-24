package fuguriprivatecoding.autotoolrecode.event;

public interface EventListener {
    boolean shouldListenEvents();
    void onEvent(Event event);

    default void registerToEvents() {
        EventCaller.getInstance().register(this);
    }
}
