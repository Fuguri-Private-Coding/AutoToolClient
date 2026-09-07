package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class DrawBlockHighlightEvent extends Event {
    @Getter
    private static final DrawBlockHighlightEvent instance = new DrawBlockHighlightEvent();
    private DrawBlockHighlightEvent() {}
}
