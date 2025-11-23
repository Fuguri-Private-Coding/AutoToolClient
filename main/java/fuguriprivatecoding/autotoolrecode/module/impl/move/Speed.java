package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "Speed", category = Category.MOVE, description = "Позволяет вам двигаться быстрее.")
public class Speed extends Module {

	int ticks;

	public Mode mode = new Mode("Mode", this)
			.addModes("45Degree", "Vanilla", "FunnyMcSkyPvp")
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

	@Override
	public void onEvent(Event event) {
		if (mc.thePlayer == null || mc.theWorld == null) return;
		switch (mode.getMode()) {
			case "45Degree" -> {
                if (Modules.getModule(Scaffold.class).isToggled() || TargetStorage.getTarget() != null) return;

                if (event instanceof TickEvent) {
                    float yaw = MoveUtils.getDir();

                    if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.onGround){
                        yaw += 45;
                    }

                    Rot rotation = new Rot(MathHelper.wrapDegree(yaw), mc.thePlayer.rotationPitch);

                    Rot.setServerRotation(rotation.fix());
                }

				if (event instanceof MoveEvent e) {
                    MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                }

                if (event instanceof MotionEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                    e.setPitch(Rot.getServerRotation().getPitch());
                }

                if (event instanceof LookEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                    e.setPitch(Rot.getServerRotation().getPitch());
                }

                if (event instanceof ChangeHeadRotationEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                    e.setPitch(Rot.getServerRotation().getPitch());
                }

                if (event instanceof UpdateBodyRotationEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                }

                if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());
                if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
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
