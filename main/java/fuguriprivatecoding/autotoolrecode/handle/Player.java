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

    public static int velocity;
    public static int hurtTime;
    public static int fallDistance;

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

            switch (packet) {
                case S12PacketEntityVelocity s12 when s12.getId() == mc.thePlayer.getEntityId() -> velocity = 20;
                default -> {}
            }
        }
    }

    private void update() {
        if (mc.thePlayer.hurtTime == 10) {
            hurtTime = 10;
        }

        if (mc.thePlayer.fallDistance > 3) {
            fallDistance = 20;
        }

        if (!mc.thePlayer.onGround) {
            airTicks++;
            groundTicks = 0;
        }

        if (mc.thePlayer.onGround) {
            groundTicks++;
            airTicks = 0;
        }

        if (fallDistance > 0) fallDistance--;
        if (velocity > 0) velocity--;
        if (hurtTime > 0) hurtTime--;
    }

    public static boolean isClutch() {
        return hurtTime > 0 || airTicks > 12;
    }
}
