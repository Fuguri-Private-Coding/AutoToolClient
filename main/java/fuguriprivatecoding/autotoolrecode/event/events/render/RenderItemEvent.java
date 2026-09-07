package fuguriprivatecoding.autotoolrecode.event.events.render;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
public class RenderItemEvent extends Event {
    @Getter
    public static final RenderItemEvent instance = new RenderItemEvent();
    private RenderItemEvent() {}

    float swingProgress, equipProgress;
}