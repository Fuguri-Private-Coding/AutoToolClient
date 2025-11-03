package fuguriprivatecoding.autotoolrecode.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;

@AllArgsConstructor
@Getter
public class ClickEvent extends CancelableEvent {
    private final Button button;

    public enum Button {
        LEFT, RIGHT, MIDDLE
    }
}
