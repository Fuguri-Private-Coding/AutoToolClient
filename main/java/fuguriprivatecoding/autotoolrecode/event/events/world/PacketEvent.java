package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import net.minecraft.network.Packet;

@Setter
@Getter
@AllArgsConstructor
public class PacketEvent extends Event {

	Packet packet;
	final long sendTime;
	final PacketDirection direction;

	public PacketEvent(Packet packet, PacketDirection direction) {
		this.packet = packet;
		sendTime = System.currentTimeMillis();
		this.direction = direction;
	}
}
