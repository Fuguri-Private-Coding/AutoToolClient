package fuguriprivatecoding.autotoolrecode.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;
import net.minecraft.entity.Entity;

@Setter
@Getter
@AllArgsConstructor
public class AttackEvent extends CancelableEvent {
	final Entity hittingEntity;
	private boolean cancelSprint;
}
