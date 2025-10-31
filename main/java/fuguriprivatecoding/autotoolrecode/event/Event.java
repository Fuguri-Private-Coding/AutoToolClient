package fuguriprivatecoding.autotoolrecode.event;

import fuguriprivatecoding.autotoolrecode.Client;

public class Event {
    public void call() {
        Client.INST.getEvents().call(this);
    }

    public void callNoWorldNoPlayer() {
        Client.INST.getEvents().callNoWorldNoPlayer(this);
    }
}
