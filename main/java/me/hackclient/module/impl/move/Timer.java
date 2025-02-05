package me.hackclient.module.impl.move;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.FloatSetting;

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
