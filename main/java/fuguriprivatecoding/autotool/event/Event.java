package fuguriprivatecoding.autotool.event;

import fuguriprivatecoding.autotool.Client;

public class Event {
    public void call() {
        Client.INST.getEventManager().call(this);
    }
}
