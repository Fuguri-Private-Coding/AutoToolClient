package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;

@ModuleInfo(name = "BWHelper", category = Category.MISC, description = "Пока не работает.")
public class BWHelper extends Module {

    MultiMode modes = new MultiMode("BWModes", this)
        .addModes("RenderBreaking")
        ;


    // TODO(ПОдумать что можно сюда добавить и нужно сюда чтото добавить.)


    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {

    }
}
