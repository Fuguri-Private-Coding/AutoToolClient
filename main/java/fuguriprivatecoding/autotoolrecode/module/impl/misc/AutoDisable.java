package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Module;

public class AutoDisable extends Module {

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {

        }
    }
}
