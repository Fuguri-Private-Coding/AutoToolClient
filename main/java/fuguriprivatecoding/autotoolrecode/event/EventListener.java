package fuguriprivatecoding.autotoolrecode.event;

public interface EventListener {
    boolean listen();
    void onEvent(Event event);
}
