package fuguriprivatecoding.autotool.utils.packet;

public class TimeUtils {
    long lastTime;

    public TimeUtils() {
        this(System.currentTimeMillis());
    }

    public TimeUtils(long lastTime) {
        this.lastTime = lastTime;
    }

    public void reset() {
        lastTime = System.currentTimeMillis();
    }

    public boolean reached(long time) {
        return reached(time, false);
    }

    public boolean reached(double time) {
        return reached(time, false);
    }

    public boolean reached(double time, boolean reset) {
        return reached((long) time, reset);
    }

    public boolean reached(long time, boolean reset) {
        boolean reached = lastTime + time <= System.currentTimeMillis();

        if (reached && reset) {
            reset();
        }

        return reached;
    }

    public static boolean reached(long lastTime, long time) {
        return lastTime + time <= System.currentTimeMillis();
    }

    public static boolean reached(long lastTime, double time) {
        return reached(lastTime, (long) time);
    }
}