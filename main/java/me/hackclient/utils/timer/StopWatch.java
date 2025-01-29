package me.hackclient.utils.timer;

public class StopWatch {

	long lastMS, lastNS;

	public StopWatch() {
		reset();
	}

	/**
	 * @return Сколько миллисекунд прошло с последнего ресета таймера
	 */
	public long reachedMS() {
		return System.currentTimeMillis() - lastMS;
	}

	/**
	 * @return Сколько наносекунд прошло с последнего ресета таймера
	 */
	public long reachedNS() {
		return System.nanoTime() - lastNS;
	}

	/**
	 * @param time Время с прошлого ресета
	 */
	public boolean reachedMS(long time) {
		return reachedMS() >= time;
	}

	/**
	 * @return Прошло ли time времени с последнего ресета
	 */
	public boolean reachedNS(long time) {
		return reachedNS() >= time;
	}

	public void reset() {
		lastNS = System.nanoTime();
		lastMS = System.currentTimeMillis();
	}
}