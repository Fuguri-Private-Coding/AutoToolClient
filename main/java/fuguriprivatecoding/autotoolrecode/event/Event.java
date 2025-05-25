package fuguriprivatecoding.autotoolrecode.event;

import fuguriprivatecoding.autotoolrecode.Client;

public class Event {
    public void call() {
        Client.INST.getEventManager().call(this);
    }
}
