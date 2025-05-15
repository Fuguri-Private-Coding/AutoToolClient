package fuguriprivatecoding.autotool.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.event.Event;
import net.minecraft.entity.EntityLivingBase;

@Setter
@Getter
@AllArgsConstructor
public class EntityKilledEvent extends Event {
    EntityLivingBase entity;
}
