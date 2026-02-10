package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class Player implements Imports, EventListener {

    public static int airTicks;
    public static int groundTicks;

    private static int velocity;
    private static int hurtTime;

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

        if (event instanceof PacketEvent e) {
            Packet packet = e.getPacket();

            if (packet instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                velocity = 20;
            }
        }
    }

    private void update() {
        if (mc.thePlayer.hurtTime == 10) {
            hurtTime = 10;
        }

        if (!mc.thePlayer.onGround) {
            airTicks++;
            groundTicks = 0;
        }

        if (mc.thePlayer.onGround) {
            groundTicks++;
            airTicks = 0;
        }

        if (velocity > 0) velocity--;
        if (hurtTime > 0) hurtTime--;
    }

    public static boolean isClutch() {
        return hurtTime > 0 || airTicks > 12;
    }
}
