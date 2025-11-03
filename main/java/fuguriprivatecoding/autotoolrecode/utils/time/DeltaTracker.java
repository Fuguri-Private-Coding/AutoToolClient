package fuguriprivatecoding.autotoolrecode.utils.time;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.lwjgl.input.Mouse;

@UtilityClass
public class DeltaTracker {
    private long prevMilli, prevNano;
    @Getter private long milli, nano;
    @Getter private int deltaScroll;

    public void startFrame() {
        prevMilli = System.currentTimeMillis();
        prevNano = System.nanoTime();
        deltaScroll = Mouse.getDWheel();
    }

    public void update() {
        milli = System.currentTimeMillis() - prevMilli;
        nano = System.nanoTime() - prevNano;
    }
}
