package me.hackclient.event;

public class CancelableEvent extends Event {
	private boolean canceled;

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
