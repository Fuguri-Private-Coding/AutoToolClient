package fuguriprivatecoding.autotoolrecode.event.events.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Setter
@Getter
public class AttackEvent extends Event {
	@Getter
	private static final AttackEvent instance = new AttackEvent();
	private AttackEvent() {}

	Entity hittingEntity;
	private boolean cancelSprint;
}
