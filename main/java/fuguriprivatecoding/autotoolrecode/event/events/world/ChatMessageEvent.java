package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.util.IChatComponent;

@Getter
@Setter
public class ChatMessageEvent extends Event {
    @Getter private static final ChatMessageEvent instance = new ChatMessageEvent();
    private ChatMessageEvent() {}

    IChatComponent message;
    Type type;

    public enum Type {
        IN_GUI, IN_CHAT
    }
}
