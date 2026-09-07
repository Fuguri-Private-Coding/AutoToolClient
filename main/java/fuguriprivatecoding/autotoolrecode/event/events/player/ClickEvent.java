package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClickEvent extends Event {
    @Getter private static final ClickEvent instance = new ClickEvent();
    private ClickEvent() {}

    private Button button;

    public enum Button {
        LEFT, RIGHT, MIDDLE
    }
}
