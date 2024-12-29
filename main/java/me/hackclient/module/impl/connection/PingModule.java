package me.hackclient.module.impl.connection;

import me.hackclient.event.Direction;
import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Ping", category = Category.CONNECTION, key = Keyboard.KEY_P)
public class PingModule extends Module {

	IntegerSetting delay = new IntegerSetting("Delay", this, 0, 1000, 500);
	IntegerSetting attackDelay = new IntegerSetting("AttackConditionTime", this, 0, 500, 500);
	IntegerSetting flagDelay = new IntegerSetting("FlagConditionTime", this, 0, 500, 500);

	// Таймер для задержки после ресета, помогает обходить античиты
	private long lastMS;
	private int stoppingTime;
	private final LinkedHashMap<Packet<?>, Long> packetBuffer;
	private final List<Doubles<Vec3, Long>> posBuffer;

	public PingModule() {
		packetBuffer = new LinkedHashMap<>();
		posBuffer = new ArrayList<>();
		stoppingTime = 0;
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof PacketEvent packetEvent) {
			Packet<?> packet = packetEvent.getPacket();
			Direction direction = packetEvent.getDirection();

			// Пропускает некоторые пакеты, также нужно для чего-то там
			if (packet instanceof C00Handshake || packet instanceof C00PacketServerQuery
					|| packet instanceof C01PacketPing || packet instanceof C01PacketChatMessage
					|| packet instanceof S01PacketPong) {
				return;
			}

			// Ресет при атаке
			if (packet instanceof C02PacketUseEntity) {
				if (((C02PacketUseEntity) packet).getAction() == C02PacketUseEntity.Action.ATTACK) {
					stoppingTime = attackDelay.getValue();
				}
			}


			// Ресет при флаге от античита/сервера, чтобы быстрее расфлагаться
			if (packet instanceof S08PacketPlayerPosLook) {
				stoppingTime = flagDelay.getValue();
			}

			if (stoppingTime > 0) {
				resetPackets();
			} else if (direction == Direction.OUTGOING) {
				packetEvent.setCanceled(true);
				packetBuffer.put(packet, System.currentTimeMillis());
				if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
					posBuffer.add(new Doubles<>(c03.getPosVec(), System.currentTimeMillis()));
				}
			}
		}

		if (event instanceof RunGameLoopEvent) {
			handleStandalone();

			if (lastMS == 0) {
				lastMS = System.currentTimeMillis();
			}

			stoppingTime -= (int) (System.currentTimeMillis() - lastMS);
			lastMS = System.currentTimeMillis();

			if (stoppingTime < 0) {
				stoppingTime = 0;
			}
		}
	}

	private void handleStandalone() {
		if (packetBuffer.isEmpty())
			return;

		mc.addScheduledTask(() -> packetBuffer.forEach( (packet, aLong) -> {
            if (System.currentTimeMillis() >= aLong + delay.getValue()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
                packetBuffer.remove(packet);
            }
        }));

		posBuffer.removeIf(doubles -> System.currentTimeMillis() >= doubles.getSecond() + delay.getValue());
	}

	private void resetPackets() {
		mc.addScheduledTask(() -> {
			packetBuffer.forEach( (packet, aLong) -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet) );
			packetBuffer.clear();
		});
	}
}
