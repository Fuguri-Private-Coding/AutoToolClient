package fuguriprivatecoding.autotool.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.event.CancelableEvent;
import fuguriprivatecoding.autotool.event.PacketDirection;
import net.minecraft.network.Packet;

@Setter
@Getter
@AllArgsConstructor
public class PacketEvent extends CancelableEvent {

	Packet packet;
	final long sendTime;
	final PacketDirection direction;

	public PacketEvent(Packet packet, PacketDirection direction) {
		this.packet = packet;
		sendTime = System.currentTimeMillis();
		this.direction = direction;
	}
}
