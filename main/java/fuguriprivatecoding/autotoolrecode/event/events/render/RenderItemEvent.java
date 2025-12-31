package fuguriprivatecoding.autotoolrecode.event.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class RenderItemEvent extends Event {
    public static final RenderItemEvent INST = new RenderItemEvent(0, 0);

    float swingProgress, equipProgress;
}