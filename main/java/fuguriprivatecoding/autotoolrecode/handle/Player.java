package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

public class Player implements Imports, EventListener {

    public static int airTicks;
    public static int groundTicks;

    public Player() {
        Events.register(this);
    }

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
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
        }

        if (mc.thePlayer.onGround) {
            groundTicks++;
            airTicks = 0;
        }
    }

}
