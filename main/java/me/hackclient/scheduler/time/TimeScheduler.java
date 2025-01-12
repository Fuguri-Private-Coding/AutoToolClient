package me.hackclient.scheduler.time;

import me.hackclient.event.callable.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.interfaces.InstanceAccess;

import java.util.ArrayList;
import java.util.List;

public class TimeScheduler implements CallableObject, InstanceAccess {
    List<Doubles<Runnable, Long>> actions;

    public TimeScheduler() {
        callables.add(this);
        actions = new ArrayList<>();
    }

    public void addAction(Runnable action, long waitTime) {
        actions.add(new Doubles<>(action, System.currentTimeMillis() + waitTime));
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            actions.forEach(runnableLongDoubles -> {
                if (System.currentTimeMillis() >= runnableLongDoubles.getSecond()) {
                    runnableLongDoubles.getFirst().run();
                    actions.remove(runnableLongDoubles);
                }
            });
        }
    }
}
