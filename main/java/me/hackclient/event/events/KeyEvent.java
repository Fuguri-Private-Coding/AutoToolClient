package me.hackclient.event.events;

import me.hackclient.event.Event;

public class KeyEvent extends Event {
	private int key;

	public KeyEvent(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}
}
