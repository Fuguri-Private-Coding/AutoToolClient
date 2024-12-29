package me.hackclient.event.events;

import me.hackclient.event.Event;

public class ChangeHeadRotationEvent extends Event {
	private float yaw, pitch;

	public ChangeHeadRotationEvent(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
}
