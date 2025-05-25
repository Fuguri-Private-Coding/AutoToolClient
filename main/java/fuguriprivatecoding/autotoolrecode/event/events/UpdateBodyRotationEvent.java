package fuguriprivatecoding.autotoolrecode.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class UpdateBodyRotationEvent extends Event {
    float yaw;
}
