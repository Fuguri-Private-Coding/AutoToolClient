package fuguriprivatecoding.autotoolrecode.event.events;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Render2DEvent extends Event {
    int width, height;
    int mouseX, mouseY;
}
