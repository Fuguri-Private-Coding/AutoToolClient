package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof AttackEvent) {
			if (mc.thePlayer.hurtTime == 0)
				return;
			if (mc.thePlayer.isSprinting()) {
				mc.thePlayer.setSprinting(false);
				mc.thePlayer.motionX *= 0.6;
				mc.thePlayer.motionZ *= 0.6;
			}
		}
	}
}
