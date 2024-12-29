package me.hackclient.event.events;

import me.hackclient.event.CancelableEvent;
import me.hackclient.event.Direction;
import net.minecraft.network.Packet;

public class PacketEvent extends CancelableEvent {

	private Packet packet;
	private final long sendTime;
	private final Direction direction;

	public PacketEvent(Packet packet, Direction direction) {
		this.packet = packet;
		sendTime = System.currentTimeMillis();
		this.direction = direction;
	}

	public Packet getPacket() {
		return packet;
	}

	public void setPacket(Packet packet) {
		this.packet = packet;
	}

	public long getSendTime() {
		return sendTime;
	}

	public Direction getDirection() {
		return direction;
	}
}
