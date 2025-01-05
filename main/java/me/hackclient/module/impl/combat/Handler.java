package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "Handler", category = Category.COMBAT, toggled = true)
public class Handler extends Module {

	private boolean rotated;

	FloatSetting yawBackRotate = new FloatSetting("YawStepBackRotate", this, 0f, 180f, 30f, 0.5f);
	FloatSetting pitchBackRotate = new FloatSetting("PitchStepBackRotate", this, 0f, 180f, 10f, 0.5f);

	private KillAura killAura;

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (killAura == null) killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);

		if (killAura.isToggled() && killAura.getTarget() != null) {
			rotated = true;
			return;
		}

		if (rotated) {
			if (event instanceof TickEvent) {
				Rotation playerRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
				Rotation serverRotation = Rotation.getServerRotation();
				Rotation delta = new Rotation(
						MathHelper.wrapDegree(playerRotation.getYaw() - serverRotation.getYaw()),
						playerRotation.getPitch() - serverRotation.getPitch()
				);

				if (delta.hypot() < 0.2) {
					rotated = false;
				} else {
					delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawBackRotate.getValue(), yawBackRotate.getValue()));
					delta.setPitch(MathHelper.clamp(delta.getPitch(), -pitchBackRotate.getValue(), pitchBackRotate.getValue()));
					delta = RotationUtils.fixDelta(delta);
					Rotation.setServerRotation(serverRotation.add(delta));
				}
			}

			if (event instanceof ChangeHeadRotationEvent event1) {
				event1.setYaw(Rotation.getServerRotation().getYaw());
				event1.setPitch(Rotation.getServerRotation().getPitch());
			}
			if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
				UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
			}

			if (event instanceof LookEvent lookEvent) {
				lookEvent.setYaw(Rotation.getServerRotation().getYaw());
				lookEvent.setPitch(Rotation.getServerRotation().getPitch());
			}

			if (event instanceof MotionEvent motionEvent) {
				motionEvent.setYaw(Rotation.getServerRotation().getYaw());
				motionEvent.setPitch(Rotation.getServerRotation().getPitch());
			}

			if (event instanceof MoveFlyingEvent moveFlyingEvent) {
				moveFlyingEvent.setCanceled(true);
				MoveUtils.silentMoveFix(moveFlyingEvent);
			}

			if (event instanceof SprintEvent) {
				if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
					mc.thePlayer.setSprinting(false);
				}
			}

			if (event instanceof JumpEvent jumpEvent) {
                jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
			}

		} else if (event instanceof TickEvent) {
			Rotation.setServerRotation(new Rotation(
					MathHelper.wrapDegree(mc.thePlayer.rotationYaw),
					mc.thePlayer.rotationPitch
			));
		}
	}

	@Override
	public boolean handleEvents() {
		return mc.theWorld != null && mc.thePlayer != null;
	}
}
