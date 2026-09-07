package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
public class KeyEvent extends Event {
	@Getter
	private static final KeyEvent instance = new KeyEvent();
	private KeyEvent() {}

	int key;
}
