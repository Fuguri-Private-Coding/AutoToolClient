package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;

@ModuleInfo(name = "IRC", category = Category.CLIENT)
public class IRC extends Module {


    @EventTarget
    public void onEvent(Event event) {

    }
}
