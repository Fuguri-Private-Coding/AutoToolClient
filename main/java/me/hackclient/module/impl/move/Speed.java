package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveEvent;
import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.move.MoveUtils;
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

	final FloatSetting timerBoostSpeed = new FloatSetting("TimerBoostSpeed", this, () -> mode.getMode().equals("FunnyMcSkyPvp"),  1.0f, 2.0f, 1.2f, 0.1f);
	final IntegerSetting tickAtBoostDown = new IntegerSetting("TickAtBoostMotionYDown", this, () -> mode.getMode().equals("FunnyMcSkyPvp"),  1, 8, 6);
	final FloatSetting boostDownAmount = new FloatSetting("BoostDownAmount", this, () -> mode.getMode().equals("FunnyMcSkyPvp"),  0, 0.05f, 0.04f, 0.01f);

	BooleanSetting resetMotion = new BooleanSetting("ResetMotionOnDisable", this, false);
	FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 2f, 1.7f, 0.1f);

	@Override
	public void onDisable() {
		super.onDisable();
		if (resetMotion.isToggled()) {
			mc.thePlayer.stopMotion();
		}
		mc.timer.timerSpeed = 1f;
		mc.thePlayer.speedInAir = 0.02f;
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
