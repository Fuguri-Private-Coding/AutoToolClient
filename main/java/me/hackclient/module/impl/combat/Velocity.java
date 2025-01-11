package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import javax.swing.table.AbstractTableModel;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"Vanilla",
			new String[] {
					"Legit",
					"Vanilla",
					"Intave",
					"Matrix",
					"Test",
			}
	);

	IntegerSetting chance = new IntegerSetting("Chance", this, 0, 100, 100);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		switch (mode.getMode()) {
			case "Legit" -> {
				if (event instanceof MoveButtonEvent moveButtonEvent && mc.thePlayer.hurtTime == 10) {
					// TODO: ДАБАВИТЬ ДЕЛАЙ И РАНДАМ, И ПЕНИС ТОЖЕ ДИСВЕС ОЦЕНИТ.
					moveButtonEvent.setJump(true);
				}
			}

			case "Vanilla" -> {
				if (event instanceof PacketEvent packetEvent
						&& packetEvent.getPacket() instanceof S12PacketEntityVelocity s12
						&& s12.getEntityID() == mc.thePlayer.getEntityId()) {
					packetEvent.setCanceled(true);
				}
			}

			case "Intave" -> {
				if (event instanceof AttackEvent && mc.thePlayer.isSprinting()) {
					mc.thePlayer.setSprinting(false);
					mc.thePlayer.motionX *= 0.6;
					mc.thePlayer.motionZ *= 0.6;
				}
			}

			case "Matrix" -> {
				if (event instanceof PacketEvent packetEvent
						&& packetEvent.getPacket() instanceof S12PacketEntityVelocity s12
						&& s12.getEntityID() == mc.thePlayer.getEntityId()
						&& mc.thePlayer.getBps(false) > 0) {
					packetEvent.setCanceled(true);
					mc.thePlayer.motionY = (double) s12.getMotionY() / 8000;
				}
			}

			case "Test" -> {
				//penis
			}
		}
	}

	@Override
	public String getSuffix() {
		return mode.getMode();
	}
}
