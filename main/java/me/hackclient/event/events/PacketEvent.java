package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.CancelableEvent;
import me.hackclient.event.PackerDirection;
import net.minecraft.network.Packet;

@Setter
@Getter
@AllArgsConstructor
public class PacketEvent extends CancelableEvent {

	Packet packet;
	final long sendTime;
	final PackerDirection direction;

	public PacketEvent(Packet packet, PackerDirection direction) {
		this.packet = packet;
		sendTime = System.currentTimeMillis();
		this.direction = direction;
	}
}
