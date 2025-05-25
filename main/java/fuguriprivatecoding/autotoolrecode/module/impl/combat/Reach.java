package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;

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
