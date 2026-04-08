package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;

@ModuleInfo(name = "CPSBooster", category = Category.LEGIT, description = "увеличивает ваш кпс, но только тогда, когда вы кликаете сами.")
public class CPSBooster extends Module {

    private final IntegerSetting chance = new IntegerSetting("Chance", this, 0, 100, 80);

    @Override
    public void onEvent(Event event) {
        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (Math.random() * 100 <= chance.getValue()) {
                Clicks.addClick();
            }
        }
    }
}
