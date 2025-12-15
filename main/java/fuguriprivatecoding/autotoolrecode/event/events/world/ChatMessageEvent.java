package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.IChatComponent;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageEvent extends Event {
    IChatComponent message;
}
