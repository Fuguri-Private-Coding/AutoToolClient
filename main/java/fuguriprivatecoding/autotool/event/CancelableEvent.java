package fuguriprivatecoding.autotool.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CancelableEvent extends Event {
	boolean canceled;

    public void cancel() {
		setCanceled(true);
	}
}
