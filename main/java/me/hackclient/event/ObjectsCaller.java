package me.hackclient.event;

public class ObjectsCaller implements IObjectCaller {
	public void onEvent(Event event) {
		for (CallableObject object : CallableObject.callables) {
			if (!object.handleEvents())
				continue;

			object.onEvent(event);
		}
	}
}
