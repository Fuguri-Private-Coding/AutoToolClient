package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Render2DEvent extends Event {
    public static final Render2DEvent INST = new Render2DEvent(null, 0,0);

    ScaledResolution sc;
    int mouseX, mouseY;
}
