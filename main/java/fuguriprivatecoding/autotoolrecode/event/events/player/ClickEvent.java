package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClickEvent extends Event {
    private final Button button;

    public enum Button {
        LEFT, RIGHT, MIDDLE
    }
}
