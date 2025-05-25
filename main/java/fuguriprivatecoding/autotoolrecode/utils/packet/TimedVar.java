package fuguriprivatecoding.autotoolrecode.utils.packet;

public class TimedVar<T> {
    private final Pair<T, Long> pair;

    public TimedVar(T var) {
        this(var, System.currentTimeMillis());
    }

    public TimedVar(T var, long time) {
        this.pair = new Pair<>(var, time);
    }

    public T getVar() {
        return pair.first();
    }

    public long getTime() {
        return pair.second();
    }
}