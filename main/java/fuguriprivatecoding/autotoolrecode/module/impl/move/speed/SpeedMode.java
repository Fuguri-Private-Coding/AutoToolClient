package fuguriprivatecoding.autotoolrecode.module.impl.move.speed;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;

public interface SpeedMode {
    void onEnable(Speed speed);
    void onDisable(Speed speed);
    void handleEvent(Event event, Speed speed);
    String getName();
}