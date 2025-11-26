package fuguriprivatecoding.autotoolrecode.event.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;
import net.minecraft.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public class DrawEntityEvent extends CancelableEvent {
    Entity drawingEntity;
}
