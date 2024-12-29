package me.hackclient.utils.timer;

public class StopWatch {
	private long lastMS;
	private long lastNS;

	public StopWatch() {
		reset();
	}

	public long reachedMS() {
		return System.currentTimeMillis() - lastMS;
	}

	public long reachedNS() {
		return System.nanoTime() - lastNS;
	}

	public void reset() {
		lastNS = System.nanoTime();
		lastMS = System.currentTimeMillis();
	}
}
