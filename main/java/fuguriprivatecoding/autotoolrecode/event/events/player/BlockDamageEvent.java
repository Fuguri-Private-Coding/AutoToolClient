package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockDamageEvent extends Event {
    @Getter
    private static final BlockDamageEvent instance = new BlockDamageEvent();

    private BlockDamageEvent() {}

    float currentDamage, addingDamage;
}
