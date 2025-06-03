package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.console.ConsoleGuiScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;

@ModuleInfo(name = "GuiMove", category = Category.MOVE)
public class GuiMove extends Module {

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MoveButtonEvent e) {
            if (mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof ConfigGuiScreen || mc.currentScreen instanceof ConsoleGuiScreen) {

            }
        }
    }
}
