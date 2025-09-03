package fuguriprivatecoding.autotoolrecode.managers;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

public class PlayerManager implements Imports {

    public static int airTicks;
    public static int groundTicks;

    public PlayerManager() {
        Client.INST.getEventManager().register(this);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof TickEvent) {
            update();
        }
    }

    private void update() {
        if (!mc.thePlayer.onGround) {
            airTicks++;
            groundTicks = 0;
        } else {
            groundTicks++;
            airTicks = 0;
        }
    }

}
