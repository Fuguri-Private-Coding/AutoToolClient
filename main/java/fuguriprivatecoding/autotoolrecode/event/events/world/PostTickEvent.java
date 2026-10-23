package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;

public class PostTickEvent extends Event {
    @Getter
    private final static PostTickEvent instance = new PostTickEvent();
    private PostTickEvent() {}

}
