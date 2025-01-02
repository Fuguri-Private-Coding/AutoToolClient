package me.hackclient.event;

public interface CallableObject extends IObjectCaller {
	void onEvent(Event event);
}
