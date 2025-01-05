package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.timer.StopWatch;

@ModuleInfo(name = "HitSelect", category = Category.COMBAT)
public class HitSelect extends Module {

    final StopWatch hitSelectTimer;

    IntegerSetting minPlayerHurtTime = new IntegerSetting("MinPlayerHurtTimeToForceHit", this, 0, 9, 6);

    public HitSelect() {
        hitSelectTimer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

    }
}
