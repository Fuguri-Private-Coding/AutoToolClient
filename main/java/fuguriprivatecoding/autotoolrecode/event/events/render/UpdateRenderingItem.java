package fuguriprivatecoding.autotoolrecode.event.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.CancelableEvent;
import net.minecraft.item.ItemStack;

@Getter
@Setter
@AllArgsConstructor
public class UpdateRenderingItem extends CancelableEvent {
    ItemStack stack;
}
