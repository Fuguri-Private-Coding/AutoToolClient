package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class ScoreboardRenderEvent extends Event {
    @Getter private static final ScoreboardRenderEvent instance = new ScoreboardRenderEvent();
    private ScoreboardRenderEvent() {}
}
