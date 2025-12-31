package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.util.IChatComponent;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEvent extends Event {
    IChatComponent message;
    Type type;

    public enum Type {
        IN_GUI, IN_CHAT
    }
}
