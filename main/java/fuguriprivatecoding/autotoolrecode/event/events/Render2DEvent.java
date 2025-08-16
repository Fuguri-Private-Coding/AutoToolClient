package fuguriprivatecoding.autotoolrecode.event.events;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@Setter
@AllArgsConstructor
public class Render2DEvent extends Event {
    ScaledResolution sc;
    int mouseX, mouseY;
}
