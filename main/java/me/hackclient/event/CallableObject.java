package me.hackclient.event;

import java.util.ArrayList;
import java.util.List;

public interface CallableObject {
	List<CallableObject> callables = new ArrayList<>();
	void onEvent(Event event);
	boolean handleEvents();
}
