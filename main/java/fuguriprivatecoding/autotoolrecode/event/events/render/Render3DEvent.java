package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class Render3DEvent extends Event {
    @Getter
    private static final Render3DEvent instance = new Render3DEvent();
    private Render3DEvent() {}
}
