package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.PackerDirection;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import tv.twitch.chat.Chat;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

	ModeSetting mode = new ModeSetting(
			"Mode",
			this,
			"Intave",
			new String[] {
					"Legit",
					"Vanilla",
					"Intave",
					"IntaveReduce",
					"Lag",
					"Matrix",
					"Test",
			}
	);

	IntegerSetting chance = new IntegerSetting("Chance", this, 0, 100, 100);
	IntegerSetting lag = new IntegerSetting("Lag", this, () -> mode.getMode().equals("Lag"), 1, 20, 5);
	IntegerSetting hurtTime = new IntegerSetting("HurtTime", this, () -> mode.getMode().equals("IntaveReduce"), 0, 10, 6);

	final List<Doubles<Packet, Long>> packetsBuffer;

	public Velocity() {
		packetsBuffer = new CopyOnWriteArrayList<>();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		resetPackets();
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		switch (mode.getMode()) {
			case "Legit" -> {
				if (event instanceof MoveButtonEvent moveButtonEvent && mc.thePlayer.hurtTime == 10) {
					moveButtonEvent.setJump(true);
				}
			}
			case "IntaveReduce" -> {
				if (event instanceof AttackEvent && mc.thePlayer.hurtTime == hurtTime.getValue()) {
					mc.thePlayer.motionX *= 0.6f;
					mc.thePlayer.motionZ *= 0.6f;
				}
			}

			case "Lag" -> {
				if (event instanceof PacketEvent packetEvent) {
					Packet packet = packetEvent.getPacket();

//					if (packet instanceof S08PacketPlayerPosLook && !packetsBuffer.isEmpty()) {
//						ClientUtils.chatLog("Flag reset");
//						synchronized (packetsBuffer) {
//							packetsBuffer.forEach(packetLongDoubles -> {
//								packetLongDoubles.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
//							});
//							packetsBuffer.clear();
//						}
//						return;
//					}

					if ((packet instanceof S12PacketEntityVelocity s12
							&& s12.getEntityID() == mc.thePlayer.getEntityId())
							|| packet instanceof S32PacketConfirmTransaction
							|| packet instanceof S00PacketKeepAlive
							|| packet instanceof S27PacketExplosion) {
						packetEvent.setCanceled(true);
						packetsBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
					}
				}
				if (event instanceof RunGameLoopEvent && !packetsBuffer.isEmpty()) {
					synchronized (packetsBuffer) {
						packetsBuffer.forEach(packetLongDoubles -> {
							if (packetLongDoubles.getFirst() instanceof S08PacketPlayerPosLook) {
								resetPackets();
							}
							if (System.currentTimeMillis() - packetLongDoubles.getSecond() >= lag.getValue() * 50L) {
								packetLongDoubles.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
								packetsBuffer.remove(packetLongDoubles);
							}
						});
					}
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
				if (mc.thePlayer.hurtTime > 0) {
					if (event instanceof AttackEvent attackEvent
					&& attackEvent.getHittingEntity() instanceof EntityLivingBase
					&& mc.thePlayer.isSprinting()) {
						mc.thePlayer.setSprinting(false);
						mc.thePlayer.motionX *= 0.6;
						mc.thePlayer.motionZ *= 0.6;
					}
					if (event instanceof MoveButtonEvent moveButtonEvent) {
						moveButtonEvent.setForward(true);
//						if (mc.thePlayer.onGround && mc.thePlayer.hurtTime > 8) {
//							moveButtonEvent.setJump(true);
//						}
					}
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

	void resetPackets() {
		packetsBuffer.forEach(c -> c.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener));
		packetsBuffer.clear();
	}

	@Override
	public String getSuffix() {
		return mode.getMode();
	}
}
