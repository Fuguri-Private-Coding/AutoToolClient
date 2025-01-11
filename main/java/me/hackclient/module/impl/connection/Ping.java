package me.hackclient.module.impl.connection;

import me.hackclient.event.PackerDirection;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Ping", category = Category.CONNECTION)
public class Ping extends Module {

	IntegerSetting delay = new IntegerSetting("Delay", this, 0, 1000, 500);
	IntegerSetting attackDelay = new IntegerSetting("AttackConditionTime", this, 0, 20, 10);
	IntegerSetting flagDelay = new IntegerSetting("FlagConditionTime", this, 0, 20, 10);
	BooleanSetting damageFlush = new BooleanSetting("DamageFlush", this, true);
	BooleanSetting guiFlush = new BooleanSetting("GuiFlush", this, true);
	BooleanSetting itemFlush = new BooleanSetting("ItemFlush", this, true);
	IntegerSetting blockPlacementDelay = new IntegerSetting("BlockPlacementConditionTime", this, 0, 20, 10);

	// Таймер для задержки после ресета, помогает обходить античиты
	int stoppingTime;
	final List<Doubles<Packet, Long>> packetBuffer;
	final List<Doubles<Vec3, Long>> posBuffer;

	@Override
	public void onDisable() {
		resetPackets();
	}

	public Ping() {
		packetBuffer = new ArrayList<>();
		posBuffer = new ArrayList<>();
		stoppingTime = 0;
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof PacketEvent packetEvent) {
			Packet<?> packet = packetEvent.getPacket();
			PackerDirection direction = packetEvent.getDirection();

			if (mc.isSingleplayer())
				return;

			// Пропускает некоторые пакеты, также нужно для чего-то там
			if (packet instanceof C00Handshake || packet instanceof C00PacketServerQuery
					|| packet instanceof C01PacketPing || packet instanceof C01PacketChatMessage
					|| packet instanceof S01PacketPong) {
				return;
			}

			// Ресет при атаке
			if (packet instanceof C02PacketUseEntity) {
				stoppingTime = attackDelay.getValue();
			}

			// Ресет при поставке блока, использовании придмета
			if (packet instanceof C08PacketPlayerBlockPlacement) {
				stoppingTime = blockPlacementDelay.getValue();
			}

			// Ресет при получении урона
			if (mc.thePlayer.hurtTime > 0 && damageFlush.isToggled()) {
				resetPackets();
			}

			if (mc.currentScreen instanceof GuiInventory && guiFlush.isToggled() || mc.currentScreen instanceof GuiContainer && guiFlush.isToggled()) {
				resetPackets();
			}

			if (mc.thePlayer.isUsingItem() && itemFlush.isToggled()) {
				resetPackets();
			}

			if (packet instanceof S08PacketPlayerPosLook) {
				stoppingTime = flagDelay.getValue();
			}

			if (stoppingTime > 0) {
				resetPackets();
			} else if (direction == PackerDirection.OUTGOING) {
				packetEvent.setCanceled(true);
				packetBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
				if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
					posBuffer.add(new Doubles<>(c03.getPosVec(), System.currentTimeMillis()));
				}
			}
		}

		if (event instanceof RunGameLoopEvent) {
			handleStandAlone();
		}

		if (event instanceof Render3DEvent && !posBuffer.isEmpty() && mc.gameSettings.thirdPersonView != 0) {
			RenderUtils.start3D();
			Vec3 dif = posBuffer.get(0).getFirst().subtract(mc.thePlayer.getPositionVector());
			AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().offset(dif).
					offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
			RenderUtils.renderHitBox(box);
			RenderUtils.stop3D();
		}

		if (event instanceof TickEvent) {
			if (stoppingTime > 0) stoppingTime--;
		}
	}
	private void handleStandAlone() {
		if (!packetBuffer.isEmpty()) {
			synchronized (packetBuffer) {
				for (Doubles<Packet, Long> packetLongDoubles : packetBuffer) {
					if (System.currentTimeMillis() - packetLongDoubles.getSecond() >= delay.getValue()) {
						mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetLongDoubles.getFirst());
						packetBuffer.remove(packetLongDoubles);
					}
				}
			}
		}
		if (!posBuffer.isEmpty()) {
			posBuffer.removeIf(doubles -> System.currentTimeMillis() >= doubles.getSecond() + delay.getValue());
		}
	}

	public void resetPackets() {
		if (!packetBuffer.isEmpty()) {
			synchronized (packetBuffer) {
				for (Doubles<Packet, Long> packetLongDoubles : packetBuffer) {
					mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetLongDoubles.getFirst());
				}
				packetBuffer.clear();
			}
		}
		posBuffer.clear();
	}
}