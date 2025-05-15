package fuguriprivatecoding.autotool.module.impl.combat;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;

@ModuleInfo(name = "Reach", category = Category.COMBAT)
public class Reach extends Module {

    final FloatSetting entityReach = new FloatSetting("EntityReach", this, 3f, 6f, 3f, 0.1f);
    final FloatSetting blockReach = new FloatSetting("BlockReach", this, 3f, 6f, 4.5f, 0.1f);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            Client.INST.getCombatManager().setEntityReach(entityReach.getValue());
            Client.INST.getCombatManager().setBlockReach(blockReach.getValue());
        }
    }
}
