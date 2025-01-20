package me.hackclient.module.impl.connection;

import me.hackclient.Client;
import me.hackclient.event.PackerDirection;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.visual.ClientShader;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.utils.animation.Animation3D;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Ping", category = Category.CONNECTION)
public class Ping extends Module {

	IntegerSetting delay = new IntegerSetting("Delay", this, 0, 1000, 500);
	IntegerSetting attackDelay = new IntegerSetting("AttackConditionTime", this, 0, 1000, 1);
	IntegerSetting sprintResetDelay = new IntegerSetting("SprintConditionTime", this, 0, 500, 1);
	IntegerSetting flagDelay = new IntegerSetting("FlagConditionTime", this, 0, 1000, 200);
	IntegerSetting velocityDelay = new IntegerSetting("VelocityConditionTime", this, 0, 1000, 1);
	BooleanSetting guiFlush = new BooleanSetting("GuiFlush", this, true);
	BooleanSetting itemFlush = new BooleanSetting("ItemFlush", this, true);
	IntegerSetting blockPlacementDelay = new IntegerSetting("BlockPlacementConditionTime", this, 0, 20, 10);

	// Таймер для задержки после ресета, помогает обходить античиты
	int nextDelay;
	final StopWatch resetTimer;
	final Animation3D animation3D;
	final List<Doubles<Packet, Long>> packetBuffer;
	ClientShader clientShader;
	final List<Doubles<Vec3, Long>> posBuffer;
	EntityOtherPlayerMP Player = null;

	@Override
	public void onDisable() {
		resetPackets();
	}

	public Ping() {
		animation3D = new Animation3D();
		packetBuffer = new CopyOnWriteArrayList<>();
		posBuffer = new CopyOnWriteArrayList<>();
		resetTimer = new StopWatch();
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (clientShader == null) {
			clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
			return;
		}
		if (event instanceof ChangeSprintEvent) {
			resetTimer.reset();
			nextDelay = sprintResetDelay.getValue();
		}
		if (event instanceof PacketEvent packetEvent) {
			Packet<?> packet = packetEvent.getPacket();
			PackerDirection direction = packetEvent.getDirection();

			if (mc.isSingleplayer())
				return;

			// Пропускает некоторые пакеты
			if (packet instanceof C01PacketChatMessage
			|| packet instanceof C00PacketServerQuery
			|| packet instanceof C00PacketLoginStart) {
				return;
			}

			// Ресет при атаке
			if (packet instanceof C02PacketUseEntity) {
				resetTimer.reset();
				nextDelay = attackDelay.getValue();
			}

			// Ресет при поставке блока, использовании придмета
			if (packet instanceof C08PacketPlayerBlockPlacement) {
				resetTimer.reset();
				nextDelay = blockPlacementDelay.getValue();
			}

			// Ресет при получении урона
			if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
				resetTimer.reset();
				nextDelay = velocityDelay.getValue();
			}

			if (mc.currentScreen instanceof GuiInventory && guiFlush.isToggled() || mc.currentScreen instanceof GuiContainer && guiFlush.isToggled()) {
				resetPackets();
			}

			if (mc.thePlayer.isUsingItem() && itemFlush.isToggled()) {
				resetPackets();
			}

			if (packet instanceof S08PacketPlayerPosLook) {
				resetTimer.reset();
				nextDelay = flagDelay.getValue();
			}

			if (resetTimer.reachedMS() < nextDelay) {
				resetPackets();
				return;
			}

			if (direction == PackerDirection.OUTGOING) {
				packetEvent.setCanceled(true);
				packetBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
				if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
					posBuffer.add(new Doubles<>(c03.getPosVec(), System.currentTimeMillis()));
				}
			}
		}

		if (event instanceof RunGameLoopEvent) {
			//if (posBuffer.isEmpty() || mc.gameSettings.thirdPersonView == 0) {
			//	mc.theWorld.removeEntityFromWorld(Player.getEntityId());
			//} else {
			//	Player = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
			//	Player.setPosition(animation3D.x, animation3D.y, animation3D.z);
			//	Player.prevRenderYawOffset = mc.thePlayer.prevRenderYawOffset;
			//	Player.rotationYaw = mc.thePlayer.rotationYaw;
			//	Player.rotationPitch = mc.thePlayer.rotationPitch;
			//	Player.rotationYawHead = mc.thePlayer.rotationYawHead;
			//	Player.rotationPitchHead = mc.thePlayer.rotationPitchHead;
			//	Player.prevRotationYaw = mc.thePlayer.prevRotationYaw;
			//	Player.prevRotationPitch = mc.thePlayer.prevRotationPitch;
			//	Player.prevLimbSwingAmount = mc.thePlayer.prevLimbSwingAmount;
			//	Player.prevSwingProgress = mc.thePlayer.prevSwingProgress;
			//	Player.swingProgress = mc.thePlayer.swingProgress;
			//	Player.limbSwingAmount = mc.thePlayer.limbSwingAmount;
			//	Player.limbSwing = mc.thePlayer.limbSwing;
			//	Player.prevCameraYaw = mc.thePlayer.prevCameraPitch;
			//	Player.prevCameraPitch = mc.thePlayer.prevCameraYaw;
			//	mc.theWorld.addEntityToWorld(-1000, Player);
			//}
			handleStandAlone();
		}

		if (event instanceof Render3DEvent && !posBuffer.isEmpty() && mc.gameSettings.thirdPersonView != 0) {
			Vec3 vec = posBuffer.get(0).getFirst();
			animation3D.endX = vec.xCoord;
			animation3D.endY = vec.yCoord;
			animation3D.endZ = vec.zCoord;
			animation3D.update(20f);

			RenderUtils.start3D();
			Vec3 diff = new Vec3(animation3D.x, animation3D.y, animation3D.z).subtract(mc.thePlayer.getPositionVector());
			AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().offset(diff).
					offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
			if (clientShader.isToggled() && clientShader.ping.isToggled()) {
				PixelReplacerUtils.addToDraw(() ->
					RenderUtils.renderHitBox(box)
				);
			} else {
				RenderUtils.renderHitBox(box);
			}
			RenderUtils.stop3D();
		}
	}

	private void handleStandAlone() {
		packetBuffer.forEach(packetLongDoubles -> {
			if (System.currentTimeMillis() - packetLongDoubles.getSecond() >= delay.getValue()) {
				mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetLongDoubles.getFirst());
				packetBuffer.remove(packetLongDoubles);
			}
		});

		posBuffer.removeIf(doubles -> System.currentTimeMillis() >= doubles.getSecond() + delay.getValue());
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