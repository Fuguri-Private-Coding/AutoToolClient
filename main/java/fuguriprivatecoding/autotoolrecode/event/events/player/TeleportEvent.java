package fuguriprivatecoding.autotoolrecode.event.events.player;


import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.C03PacketPlayer;

@Getter
@Setter
@AllArgsConstructor
public final class TeleportEvent extends CancelableEvent {

    private C03PacketPlayer response;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

}