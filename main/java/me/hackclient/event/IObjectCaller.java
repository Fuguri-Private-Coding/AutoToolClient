package me.hackclient.event;

import me.hackclient.event.callable.CallableObject;

import java.util.ArrayList;
import java.util.List;

public interface IObjectCaller {
    List<CallableObject> callables = new ArrayList<>();
    void onEvent(Event event);
}
