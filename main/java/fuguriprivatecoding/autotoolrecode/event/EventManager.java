package fuguriprivatecoding.autotoolrecode.event;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager implements Imports {
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (declaredMethod.getParameterCount() != 1 || !declaredMethod.getParameterTypes()[0].isAssignableFrom(Event.class)) {
                continue;
            }

            subscribers.add(new Subscriber(object, declaredMethod));
        }
    }

    public void unregister(Object object) {
        subscribers.removeIf(subscriber -> subscriber.object() == object);
    }

    public void call(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        for (Subscriber subscriber : subscribers) {
            if (!subscriber.method().getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                continue;
            }

            try {
                subscriber.method().invoke(subscriber.object(), event);
            } catch (IllegalAccessException | InvocationTargetException _) {
            }
        }
    }

    private record Subscriber(Object object, Method method) {}
}
