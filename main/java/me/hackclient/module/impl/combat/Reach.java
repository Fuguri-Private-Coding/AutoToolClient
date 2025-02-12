package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;

@ModuleInfo(
        name = "Reach",
        category = Category.COMBAT
)
public class Reach extends Module {

    final FloatSetting entityReach = new FloatSetting("EntityReach", this, 3f, 6f, 3f, 0.1f);
    final FloatSetting blockReach = new FloatSetting("BlockReach", this, 3f, 6f, 4.5f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            Client.INSTANCE.getCombatManager().setEntityReach(isToggled() ? entityReach.getValue() : 3d);
            Client.INSTANCE.getCombatManager().setBlockReach(isToggled() ? blockReach.getValue() : 4.5d);
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
