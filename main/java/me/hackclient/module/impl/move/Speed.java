package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveEvent;
import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Speed", category = Category.MOVE, key = Keyboard.KEY_V)
public class Speed extends Module {

	int ticks;

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"45Degree",
			new String[] {
					"45Degree",
					"Vanilla",
					"FunnyMcSkyPvp"
			}
	);

	BooleanSetting resetMotion = new BooleanSetting("ResetMotionOnDisable", this, false);
	FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 2f, 1.7f, 0.1f) {};

	@Override
	public void onDisable() {
		super.onDisable();
		if (resetMotion.isToggled()) {
			mc.thePlayer.stopMotion();
		}
		mc.timer.timerSpeed = 1f;
		mc.thePlayer.speedInAir = 0.02f;
		ticks = 0;
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		switch (mode.getMode()) {
			case "45Degree" -> {
				if (event instanceof MoveFlyingEvent moveFlyingEvent) {
					if (moveFlyingEvent.getForward() > 0f) {
						moveFlyingEvent.setYaw(mc.thePlayer.rotationYaw - 45f);
					}
				}

				if (event instanceof MoveEvent moveEvent) {
					if (moveEvent.getForward() > 0f) {
						moveEvent.setStrafe(moveEvent.getStrafe() - 1f);
					}
				}
			}
			case "Vanilla" -> {
				double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
				mc.thePlayer.motionX = -Math.sin(yaw) * speed.getValue();
				mc.thePlayer.motionZ = Math.cos(yaw) * speed.getValue();
			}
			case "FunnyMcSkyPvp" -> {
				if (event instanceof UpdateEvent) {
					if (mc.thePlayer.onGround) {
						ticks = 0;
						mc.thePlayer.jump();
						mc.thePlayer.motionY = 0.4D;
					} else {
						if (ticks < 10) {
							double[] motions = new double[]{0, 0, 0, 0, 0.1912, 0.3, 1, 0, 0, 0, 0};
							double motion = motions[ticks++];
							mc.thePlayer.motionY -= motion;
						}
						if (mc.thePlayer.isBurning()) {
							mc.thePlayer.motionY = -1;
						}
					}
				}
			}
		}
	}

	@Override
	public String getSuffix() {
		return mode.getMode();
	}
}
