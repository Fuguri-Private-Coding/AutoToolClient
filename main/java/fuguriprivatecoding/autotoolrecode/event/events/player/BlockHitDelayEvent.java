package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockHitDelayEvent extends Event {
    @Getter
    private static final BlockHitDelayEvent instance = new BlockHitDelayEvent();
    private BlockHitDelayEvent() {}
    int delay;
}
