package fuguriprivatecoding.autotoolrecode.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;
import net.minecraft.entity.EntityLivingBase;

@Setter
@Getter
@AllArgsConstructor
public class EntityKilledEvent extends Event {
    EntityLivingBase entity;
}
