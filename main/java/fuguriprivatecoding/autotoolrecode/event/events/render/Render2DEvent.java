package fuguriprivatecoding.autotoolrecode.event.events.render;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@Setter
public class Render2DEvent extends Event {
    @Getter
    private static final Render2DEvent instance = new Render2DEvent();
    private Render2DEvent() {}

    ScaledResolution scaledResolution;
    int mouseX, mouseY;
}
