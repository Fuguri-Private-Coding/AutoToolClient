package fuguriprivatecoding.autotoolrecode.module.impl.move.speed.impl;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.AbstractSpeedMode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;

public class VanillaMode extends AbstractSpeedMode {
    
    public VanillaMode() {
        super("Vanilla");
    }
    
    @Override
    public void handleEvent(Event event, Speed speed) {
        if (MoveUtils.isMoving()) {
            if (event instanceof MoveButtonEvent e && speed.jump.isToggled()) e.setJump(true);
            if (event instanceof MoveEvent) MoveUtils.setSpeed(0.1f * speed.speed.getValue(), true);
        }
    }
}