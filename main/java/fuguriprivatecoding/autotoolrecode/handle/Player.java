package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class Player implements Imports, EventListener {

    @Getter
    private static final Player instance = new Player();
    private Player() {}

    public int airTicks;
    public int groundTicks;

    public int velocity;
    public int hurtTime;
    public int fallDistance;

    public void init() {
        registerToEvents();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof TickEvent)
            tick();

        if (event instanceof PacketEvent e) {
            if (e.getPacket() instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                velocity = 20;
            }
        }
    }

    @Override
    public boolean shouldListenEvents() {
        return Utils.isWorldLoaded();
    }

    private void tick() {
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

    public boolean isClutch() {
        return hurtTime > 0 || airTicks > 12;
    }
}
