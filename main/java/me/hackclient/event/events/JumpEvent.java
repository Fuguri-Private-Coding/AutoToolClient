package me.hackclient.event.events;

import me.hackclient.event.CancelableEvent;

public class JumpEvent extends CancelableEvent {
	private float yaw, height;

	public JumpEvent(float yaw, float height) {
		this.yaw = yaw;
		this.height = height;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
}
