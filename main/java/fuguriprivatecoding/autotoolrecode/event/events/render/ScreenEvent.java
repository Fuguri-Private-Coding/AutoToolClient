package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;

@Getter
@Setter
@AllArgsConstructor
public class ScreenEvent extends Event {
    public static final ScreenEvent INST = new ScreenEvent(Type.PRE);

    Type type;

    public enum Type {
        PRE, POST
    }

}
