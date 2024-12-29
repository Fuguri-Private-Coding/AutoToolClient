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
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT, toggled = true)
public class MoreKBModule extends Module {

	private final StopWatch stopWatch;
	private KillAuraModule killAura;
	public int ticks, delayTicks;

	public MoreKBModule() {
		stopWatch = new StopWatch();
	}

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"LegitFast",
			new String[] {
					"LegitFast",
					"Legit",
					"One",
			}
	);

	//BooleanSetting legitFast = new BooleanSetting("LegitFast", this, true);
	//BooleanSetting legit = new BooleanSetting("Legit", this, false);

	IntegerSetting MinDelayTicks = new IntegerSetting("MinDelayTicks", this, 1, 5, 3);
	IntegerSetting MaxDelayTicks = new IntegerSetting("MaxDelayTicks", this, 1, 5, 3);
	IntegerSetting MinresetTicks = new IntegerSetting("MinResetTicks", this, 1, 5, 1);
	IntegerSetting MaxresetTicks = new IntegerSetting("MaxResetTicks", this, 1, 5, 2);
	BooleanSetting debug = new BooleanSetting("Debug", this, true);
	BooleanSetting testSprintFix2 = new BooleanSetting("TestSprintFix2", this, true);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof Render2DEvent && debug.isToggled()) {
			if (delayTicks > 0) {
				mc.fontRendererObj.drawString("waiting delay", 100, 100, -1, true);
			} else if (ticks > 0) {
				mc.fontRendererObj.drawString("reseting", 100, 100, -1, true);
			}
		}

		if (killAura == null)
			killAura = Client.INSTANCE.getModuleManager().getModule(KillAuraModule.class);

		if (killAura.getTarget() != null && killAura.getTarget().hurtTime == 10 && ticks == 0 && event instanceof TickEvent) {
			delayTicks = RandomUtils.nextInt(MinDelayTicks.getValue(), MaxDelayTicks.getValue());
			ticks = RandomUtils.nextInt(MinresetTicks.getValue(), MaxresetTicks.getValue());
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
			ticks--;
			e.setForward(false);
		}
	}

	private void handleOne(Event event) {
		if (event instanceof UpdateEvent) {
			ticks--;
			mc.thePlayer.setSprinting(true);
			if (testSprintFix2.isToggled()) {
				mc.thePlayer.setServerSprintState(true);
			}
			mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
		}
	}

	private void handleLegitFast(Event event) {
		if (event instanceof SprintEvent) {
			if (mc.thePlayer.isSprinting()) {
				mc.thePlayer.setSprinting(false);
				mc.thePlayer.setServerSprintState(false);
				ticks--;
			}
		}
	}
}
