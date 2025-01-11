package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveEvent;
import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Speed", category = Category.MOVE, key = Keyboard.KEY_V)
public class Speed extends Module {

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"45Degree",
			new String[] {
					"45Degree",
					"Vanilla"
			}
	);

	BooleanSetting resetMotion = new BooleanSetting("ResetMotionOnDisable", this, true);
	FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 2f, 1.7f, 0.1f);

	@Override
	public void onDisable() {
		super.onDisable();
		if (resetMotion.isToggled()) {
			mc.thePlayer.stopMotion();
		}
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		switch (mode.getMode()) {
			case "45Degree" : {
				if (event instanceof MoveFlyingEvent moveFlyingEvent) {
					if (moveFlyingEvent.getForward() > 0) {
						moveFlyingEvent.setYaw(mc.thePlayer.rotationYaw - 45);
					}
				}

				if (event instanceof MoveEvent moveEvent) {
					if (moveEvent.getForward() > 0) {
						moveEvent.setStrafe(moveEvent.getStrafe() - 1);
					}
				}
				break;
			}
			case "Vanilla" : {
				double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
				mc.thePlayer.motionX = -Math.sin(yaw) * speed.getValue();
				mc.thePlayer.motionZ = Math.cos(yaw) * speed.getValue();
				break;
			}
		}
	}

	@Override
	public String getSuffix() {
		return mode.getMode();
	}
}
