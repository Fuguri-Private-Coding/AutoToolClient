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

    public void call() {
        Events.call(this);
    }

    public void callNoWorldNoPlayer() {
        Events.callNoWorldNoPlayer(this);
    }
}
