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

    final FloatSetting reach = new FloatSetting("Reach", this, 3f, 6f, 3f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            Client.INSTANCE.getCombatManager().setReach(isToggled() ? reach.getValue() : 3d);
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
