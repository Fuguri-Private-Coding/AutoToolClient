package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT, toggled = true)
public class MoreKB extends Module {

    private KillAura killAura;
	public int ticks, delayTicks;

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"LegitFast",
			new String[] {
					"LegitFast",
					"Legit",
					"One"
			}
	);

	IntegerSetting MinDelayTicks = new IntegerSetting("MinDelayTicks", this, 1, 5, 3);
	IntegerSetting MaxDelayTicks = new IntegerSetting("MaxDelayTicks", this, 1, 5, 3);
	IntegerSetting MinResetTicks = new IntegerSetting("MinTicks", this, 1, 5, 1);
	IntegerSetting MaxResetTicks = new IntegerSetting("MaxTicks", this, 1, 5, 1);
	BooleanSetting debug = new BooleanSetting("Debug", this, false);
	BooleanSetting serverSprintToggle = new BooleanSetting("ServerSprintToggle", this, () -> mode.getMode().equalsIgnoreCase("One"), true);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof RunGameLoopEvent && debug.isToggled()) {
			if (delayTicks > 0) {
				ClientUtils.chatLog("Delaying ticks " + delayTicks);
			}

			if (ticks > 0) {
				ClientUtils.chatLog("Resetting ticks " + ticks);
			}
		}

		if (killAura == null) {
			killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
			ticks = 0;
			delayTicks = 0;
		}

		if (killAura.getTarget() != null && killAura.getTarget().hurtTime == 10 && ticks == 0 && event instanceof TickEvent) {
			delayTicks = RandomUtils.nextInt(MinDelayTicks.getValue(), MaxDelayTicks.getValue());
			ticks = RandomUtils.nextInt(MinResetTicks.getValue(), MaxResetTicks.getValue());
		}

		if (delayTicks > 0) {
			if (event instanceof TickEvent) {
				delayTicks--;
			}
			return;
		}

		if (ticks > 0) {
			switch (mode.getMode()) {
				case "LegitFast": {
					handleLegitFast(event);
					break;
				}
				case "One": {
					handleOne(event);
					break;
				}
				case "Legit": {
					handleLegit(event);
					break;
				}
			}
		}
	}

	private void handleLegit(Event event) {
		if (event instanceof MoveButtonEvent e) {
			e.setForward(false);
			ticks--;
		}
	}

	private void handleOne(Event event) {
		if (event instanceof UpdateEvent) {
			mc.thePlayer.setSprinting(true);
			if (serverSprintToggle.isToggled()) {
				mc.thePlayer.setServerSprintState(true);
			}
			mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
			ticks--;
		}
	}

	private void handleLegitFast(Event event) {
		if (event instanceof TickEvent) {
			if (mc.thePlayer.isSprinting()) {
				mc.thePlayer.setSprinting(false);
				mc.thePlayer.setServerSprintState(false);
				ticks--;
			}
		}
	}
}
