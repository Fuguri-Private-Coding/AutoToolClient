package me.hackclient.event.callable;

import me.hackclient.event.Event;
import me.hackclient.event.IObjectCaller;

public interface CallableObject extends IObjectCaller {
	void onEvent(Event event);
}
