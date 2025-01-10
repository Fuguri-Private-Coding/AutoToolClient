package me.hackclient.module.impl.combat;

import lombok.Getter;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.combat.killaura.click.ClickManager;
import me.hackclient.module.impl.combat.killaura.click.IClickingCFG;
import me.hackclient.module.impl.combat.killaura.rotation.IKillAuraRotation;
import me.hackclient.module.impl.combat.killaura.rotation.impl.IntaveRotation;
import me.hackclient.module.impl.combat.killaura.target.TargetSelector;
import me.hackclient.module.impl.visual.Animations;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, key = Keyboard.KEY_R)
public class KillAura extends Module {

    @Getter
    EntityLivingBase target;

	final List<Rotation> rotations;
	final TargetSelector targetSelector;
	public final ClickManager clickManager;

	IntegerSetting minCps = new IntegerSetting("MinCps", this, 1,20, 18);
	IntegerSetting maxCps = new IntegerSetting("MaxCps", this, 1,20, 18);

	BooleanSetting clickFix = new BooleanSetting("ClickFix", this, true);
	BooleanSetting mineBlazeFix = new BooleanSetting("MineBlazeFix", this, true);
	IntegerSetting minPlayerHurtTime = new IntegerSetting("MinPlayerHurtTime", this, () -> mineBlazeFix.isToggled(), 0, 9, 7);
	IntegerSetting minRandomizeDelay = new IntegerSetting("MinRandomizeDelay", this, () -> mineBlazeFix.isToggled(), 0, 200, 0);
	IntegerSetting maxRandomizeDelay = new IntegerSetting("MaxRandomizeDelay", this, () -> mineBlazeFix.isToggled(), 0, 200, 50);

	BooleanSetting moveFix = new BooleanSetting("SilentMoveFix", this, true);
	BooleanSetting fakeAutoBlock = new BooleanSetting("FakeAutoBlock", this, true);

	FloatSetting maxRange = new FloatSetting("MaxRange", this, 3f, 6f, 6f, 0.1f);
	IntegerSetting reactionTime = new IntegerSetting("ReactionTime", this, 0, 5, 1);
    FloatSetting speedYawRotation = new FloatSetting("MaxYawStep", this, 1f, 180f, 75f, 0.5f);
	FloatSetting speedPitchRotation = new FloatSetting("MaxPitchStep", this, 1f, 180f, 45f, 0.5f);

	public KillAura() {
		clickManager = new ClickManager();
		targetSelector = new TargetSelector();
		rotations = new ArrayList<>();
	}

	public void onEnable() {
		target = null;
	}

	public void onDisable() {
		rotations.clear();
		target = null;
		Animations.setAnimate(false);
	}

    @Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof TickEvent) {
			target = targetSelector.selectPlayer(maxRange.getValue());
		}
		if (target != null) {
			Animations.setAnimate(fakeAutoBlock.isToggled());
			if (event instanceof RunGameLoopEvent) {
				clickManager.checkTime();
			}

			if (event instanceof MotionEvent motionEvent) {
				motionEvent.setYaw(Rotation.getServerRotation().getYaw());
				motionEvent.setPitch(Rotation.getServerRotation().getPitch());
			}

			if (event instanceof LookEvent lookEvent) {
				lookEvent.setYaw(Rotation.getServerRotation().getYaw());
				lookEvent.setPitch(Rotation.getServerRotation().getPitch());
			}

			if (moveFix.isToggled()) {
				if (event instanceof MoveFlyingEvent moveFlyingEvent) {
					moveFlyingEvent.setCanceled(true);
					MoveUtils.silentMoveFix(moveFlyingEvent);
				}
				if (event instanceof SprintEvent) {
					if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
						mc.thePlayer.setSprinting(false);
					}
				}
			} else if (event instanceof MoveFlyingEvent moveFlyingEvent) {
				moveFlyingEvent.setYaw(Rotation.getServerRotation().getYaw());
			}

			if (event instanceof JumpEvent jumpEvent) {
				jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
			}

			if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
				changeHeadRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
				changeHeadRotationEvent.setPitch(Rotation.getServerRotation().getPitch());
			}
			if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
				UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
			}

			if (event instanceof LegitClickTimingEvent) {
				clickManager.click(target, new IClickingCFG() {
					@Override
					public boolean clickFix() {
						return clickFix.isToggled();
					}

					@Override
					public boolean mineBlazeKbFix() {
						return mineBlazeFix.isToggled();
					}

					@Override
					public int minPlayerHurtTime() {
						return minPlayerHurtTime.getValue();
					}

					@Override
					public int minCps() {
						return minCps.getValue();
					}

					@Override
					public int maxCps() {
						return maxCps.getValue();
					}

					@Override
					public int minRandomizeDelay() {
						return minRandomizeDelay.getValue();
					}

					@Override
					public int maxRandomizeDelay() {
						return maxRandomizeDelay.getValue();
					}
				});
			}
			if (event instanceof TickEvent) {
                IKillAuraRotation selectedRotation = new IntaveRotation();
				if (mc.currentScreen == null) {
					rotations.add(selectedRotation.compute(getLastRotation(), target, speedYawRotation.getValue(), speedPitchRotation.getValue()));

					Rotation.setServerRotation(rotations.get(0));

					while (rotations.size() > reactionTime.getValue()) {
						rotations.remove(0);
					}
				}
            }
		} else {
			Animations.setAnimate(false);
		}
	}

	private Rotation getLastRotation() {
		if (rotations.isEmpty()) {
			return Rotation.getServerRotation();
		} else {
			return rotations.get(rotations.size() - 1);
		}
	}
}
