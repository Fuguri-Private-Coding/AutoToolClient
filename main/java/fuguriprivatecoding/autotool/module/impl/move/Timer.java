package fuguriprivatecoding.autotool.module.impl.move;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;

@ModuleInfo(name = "Timer", category = Category.MOVE)
public class Timer extends Module {

	FloatSetting timerSpeed = new FloatSetting("TimerSpeed", this, 0.1f, 10f, 2f, 0.1f) {};

	public void onEnable() {
		mc.timer.timerSpeed = timerSpeed.getValue();
	}

	public void onDisable() {
		mc.timer.timerSpeed = 1.0F;
	}

	@Override
	public String getSuffix() {
		return String.valueOf(timerSpeed.getValue());
	}
}
