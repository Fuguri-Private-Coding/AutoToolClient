package fuguriprivatecoding.autotool.module.impl.move;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.MoveEvent;
import fuguriprivatecoding.autotool.event.events.MoveFlyingEvent;
import fuguriprivatecoding.autotool.event.events.UpdateEvent;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import fuguriprivatecoding.autotool.event.events.*;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.*;
import fuguriprivatecoding.autotool.utils.move.MoveUtils;

@ModuleInfo(name = "Speed", category = Category.MOVE)
public class Speed extends Module {

	int ticks;

	Mode mode = new Mode("Mode", this)
			.addModes("45Degree", "Vanilla")
			.setMode("45Degree");

	CheckBox resetMotion = new CheckBox("ResetMotionOnDisable", this, false);
	FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 1f, 10f, 5f, 0.1f) {};

	@Override
	public void onDisable() {
		super.onDisable();
		if (resetMotion.isToggled()) mc.thePlayer.stopMotion();
		mc.timer.timerSpeed = 1f;
		mc.thePlayer.speedInAir = 0.02f;
		ticks = 0;
	}

	@EventTarget
	public void onEvent(Event event) {
		if (mc.thePlayer == null || mc.theWorld == null) return;
		switch (mode.getMode()) {
			case "45Degree" -> {
				if (event instanceof MoveFlyingEvent moveFlyingEvent && moveFlyingEvent.getForward() > 0f) {
					moveFlyingEvent.setYaw(mc.thePlayer.rotationYaw - 45f);
				}

				if (event instanceof MoveEvent moveEvent && moveEvent.getForward() > 0f) {
					moveEvent.setStrafe(moveEvent.getStrafe() - 1f);
				}
			}
			case "Vanilla" -> {
				if (MoveUtils.isMoving()) {
					MoveUtils.setSpeed(0.1f * speed.getValue(), true);
				}
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
						if (mc.thePlayer.isBurning()) mc.thePlayer.motionY = -1;
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
