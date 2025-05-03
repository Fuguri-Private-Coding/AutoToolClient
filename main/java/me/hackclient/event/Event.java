package me.hackclient.event;

import me.hackclient.Client;

public class Event {
    public void call() {
        Client.INSTANCE.getEventManager().call(this);
    }
}
