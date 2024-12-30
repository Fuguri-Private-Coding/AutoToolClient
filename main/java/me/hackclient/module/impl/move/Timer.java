package me.hackclient.module.impl.move;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "Timer", category = Category.MOVE)
public class Timer extends Module {

	public void onEnable() {
		mc.timer.timerSpeed = 1.6F;
	}

	public void onDisable() {
		mc.timer.timerSpeed = 1.0F;
	}
}
