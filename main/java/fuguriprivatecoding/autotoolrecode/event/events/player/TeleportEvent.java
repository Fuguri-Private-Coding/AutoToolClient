package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.C03PacketPlayer;

@Getter
@Setter
public final class TeleportEvent extends Event {
    @Getter
    private static final TeleportEvent instance = new TeleportEvent();
    private TeleportEvent() {}

    private C03PacketPlayer response;
    private double x, y, z;
    private float yaw, pitch;
}