package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;

@ModuleInfo(name = "MoreSwing", category = Category.VISUAL, description = "Не останавливаясь дергает рукой #типа-бьет.")
public class MoreSwing extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent && TargetStorage.getTarget() != null) mc.thePlayer.swingItemNoPacket();
    }
}
