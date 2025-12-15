package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Setter
@Getter
@AllArgsConstructor
public class AttackEvent extends Event {
	final Entity hittingEntity;
	private boolean cancelSprint;
}
