package fuguriprivatecoding.autotool.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class RenderItemEvent extends Event {
    float swingProgress, equipProgress;
}