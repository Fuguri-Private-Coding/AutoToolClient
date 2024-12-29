package me.hackclient.event.events;

import me.hackclient.event.Event;

public class MoveEvent extends Event {
	private float forward, strafe;

	public MoveEvent(float forward, float strafe) {
		this.forward = forward;
		this.strafe = strafe;
	}

	public float getForward() {
		return forward;
	}

	public void setForward(float forward) {
		this.forward = forward;
	}

	public float getStrafe() {
		return strafe;
	}

	public void setStrafe(float strafe) {
		this.strafe = strafe;
	}
}
