package me.hackclient.event;

import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.utils.interfaces.InstanceAccess;

public class ObjectsCaller implements IObjectCaller, InstanceAccess {

	@Override
	public void onEvent(final Event event) {
		callables.forEach(callableObject -> {
			if (callableObject instanceof ConditionCallableObject conditionCallableObject) {
				if (conditionCallableObject.handleEvents())  conditionCallableObject.onEvent(event);
			} else {
				callableObject.onEvent(event);
			}
		});
	}
}
