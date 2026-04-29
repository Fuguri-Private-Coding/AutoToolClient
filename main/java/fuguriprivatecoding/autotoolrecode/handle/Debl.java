package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;

public class Debl implements EventListener {

    public Debl() {
        Events.register(this);
    }

    @Override
    public boolean listen() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof KeyEvent e) {
            Modules.getModules().forEach(module -> {
                if (module.getKey() == e.getKey()) module.toggle();
            });
        }
    }

}


