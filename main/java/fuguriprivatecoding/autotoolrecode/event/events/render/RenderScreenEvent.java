package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class RenderScreenEvent extends Event {
    @Getter
    private static final RenderScreenEvent instance = new RenderScreenEvent();
    private RenderScreenEvent() {}
}
