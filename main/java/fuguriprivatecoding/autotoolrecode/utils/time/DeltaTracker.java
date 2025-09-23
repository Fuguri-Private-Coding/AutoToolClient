package fuguriprivatecoding.autotoolrecode.utils.time;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeltaTracker {
    private long prevMilli, prevNano;
    @Getter private long milli, nano;

    public void startFrame() {
        prevMilli = System.currentTimeMillis();
        prevNano = System.nanoTime();
    }

    public void update() {
        milli = System.currentTimeMillis() - prevMilli;
        nano = System.nanoTime() - prevNano;
    }
}
