package me.hackclient.event;

import me.hackclient.utils.interfaces.InstanceAccess;

public class ObjectsCaller implements IObjectCaller, InstanceAccess {

	@Override
	public void onEvent(Event event) {
		for (CallableObject object : callables) {
			if (object instanceof ConditionCallableObject condition) {
				if (condition.handleEvents()) condition.onEvent(event);
			} else {
				object.onEvent(event);
			}
		}
	}
}
