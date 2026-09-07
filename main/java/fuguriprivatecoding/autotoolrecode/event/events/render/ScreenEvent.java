package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;

@Getter
@Setter
public class ScreenEvent extends Event {
    @Getter
    private static final ScreenEvent instance = new ScreenEvent();
    private ScreenEvent() {}

    Type type;

    public enum Type {
        PRE, POST
    }

}
