package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;

@ModuleInfo(name = "HUD", category = Category.VISUAL)
public class HUD extends Module {


    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {

        }
    }
}
