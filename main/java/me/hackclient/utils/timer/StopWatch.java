package me.hackclient.utils.timer;

public class StopWatch {
	long lastMS, lastNS;

	public StopWatch() {
		reset();
	}

	public long reachedMS() {
		return System.currentTimeMillis() - lastMS;
	}

	public long reachedNS() {
		return System.nanoTime() - lastNS;
	}

	public long reachedS() {
		return reachedMS() / 1000;
	}

	public boolean reachedMS(long time) {
		return reachedMS() >= time;
	}

	public boolean reachedNS(long time) {
		return reachedNS() >= time;
	}

	public void reset() {
		lastNS = System.nanoTime();
		lastMS = System.currentTimeMillis();
	}
}