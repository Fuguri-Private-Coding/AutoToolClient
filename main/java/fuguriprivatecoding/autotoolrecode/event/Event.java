package fuguriprivatecoding.autotoolrecode.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Event {
    boolean canceled;

    public void cancel() {
        setCanceled(true);
    }

    public void call(boolean onlyInWorld) {
        setCanceled(false);
        Events.call(this, onlyInWorld);
    }

    public void call() {
        call(true);
    }
}
