package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;

@ModuleInfo(name = "Timer", category = Category.MOVE)
public class Timer extends Module {

	FloatSetting timerSpeed = new FloatSetting("TimerSpeed", this, 0.1f, 10f, 2f, 0.1f) {};

	public void onDisable() {
		mc.timer.timerSpeed = 1.0F;
	}

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof TickEvent) {
			if (mc.timer.timerSpeed == 1) {
				mc.timer.timerSpeed = timerSpeed.getValue();
			}
		}
	}

	@Override
	public String getSuffix() {
		return String.valueOf(timerSpeed.getValue());
	}
}
