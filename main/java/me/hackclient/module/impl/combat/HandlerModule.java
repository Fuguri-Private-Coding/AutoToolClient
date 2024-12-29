package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "Handler", category = Category.COMBAT, toggled = true)
public class HandlerModule extends Module {

	private boolean rotated;

	private KillAuraModule killAura;

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (killAura == null) killAura = Client.INSTANCE.getModuleManager().getModule(KillAuraModule.class);

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
					delta.setYaw(MathHelper.clamp(delta.getYaw(), -30, 30));
					delta.setPitch(MathHelper.clamp(delta.getPitch(), -10, 10));
					delta = RotationUtils.fixDelta(delta);
					Rotation.setServerRotation(serverRotation.add(delta));
				}
			}
			if (event instanceof ChangeHeadRotationEvent event1) {
				event1.setYaw(Rotation.getServerRotation().getYaw());
				event1.setPitch(Rotation.getServerRotation().getPitch());
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
				moveFlyingEvent.setYaw(Rotation.getServerRotation().getYaw());
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
