package fuguriprivatecoding.autotoolrecode.event;

public class Event {
    public void call() {
        Events.call(this);
    }

    public void callNoWorldNoPlayer() {
        Events.callNoWorldNoPlayer(this);
    }
}
