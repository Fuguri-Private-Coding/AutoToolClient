package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
public class PreAttackEvent extends Event {
    @Getter
    private static final PreAttackEvent instance = new PreAttackEvent();
    private PreAttackEvent() {}

    Entity hittingEntity;
}
