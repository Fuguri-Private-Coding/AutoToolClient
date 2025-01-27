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
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.utils.animation.Animation3D;
import me.hackclient.utils.client.ClientUtils;
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
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Ping", category = Category.CONNECTION)
public class Ping extends Module {

	IntegerSetting delay = new IntegerSetting("Delay", this, 50, 1000, 500);

	MultiBooleanSetting flushes = new MultiBooleanSetting("FlushConditions", this)
			.add("Attack", false)
			.add("SprintChange", true)
			.add("Flag", true)
			.add("Velocity", false)
			.add("OpenedInv", true)
			.add("UsingItem", true)
			.add("BlockPlace", true)
			.add("WorldChange", false);

	IntegerSetting attackFlush = new IntegerSetting("AttackTime", this, () -> flushes.get("Attack"), 10, 1000, 20);
	IntegerSetting sprintFlush = new IntegerSetting("SprintChangeTime", this, () -> flushes.get("SprintChange"), 10, 1000, 100);
	IntegerSetting flagFlush = new IntegerSetting("FlagTime", this, () -> flushes.get("Flag"), 10, 1000, 200);
	IntegerSetting velocityFlush = new IntegerSetting("VelocityTime", this, () -> flushes.get("Velocity"), 10, 1000, 500);
	IntegerSetting openInvFlush = new IntegerSetting("OpenedInvTime", this, () -> flushes.get("OpenedInv"), 10, 1000, 50);
	IntegerSetting usingItemFlush = new IntegerSetting("UsingItemTime", this, () -> flushes.get("UsingItem"), 10, 1000, 50);
	IntegerSetting blockPlaceFlush = new IntegerSetting("BlockPlaceTime", this, () -> flushes.get("BlockPlace"), 10, 1000, 50);
	IntegerSetting worldChangeFlush = new IntegerSetting("WorldChangeTime", this, () -> flushes.get("WorldChange"), 10, 1000, 50);

	BooleanSetting onlyThirdPerson = new BooleanSetting("OnlyThirdPerson", this, true);

	int nextDelay;
	final StopWatch timer;
	final Animation3D animation3D;
	final public List<Doubles<Packet, Long>> packetBuffer;
	ClientShader clientShader;
	final List<Doubles<Vec3, Long>> posBuffer;
	EntityOtherPlayerMP player;

	@Override
	public void onDisable() {
		resetPackets();
	}

	public Ping() {
		animation3D = new Animation3D();
		packetBuffer = new CopyOnWriteArrayList<>();
		posBuffer = new CopyOnWriteArrayList<>();
		timer = new StopWatch();
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (clientShader == null) {
			clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
			return;
		}
		if (event instanceof WorldChangeEvent) {
			player = null;
			if (flushes.get("WorldChange")) {
				resetPackets();
				nextDelay = worldChangeFlush.getValue();
			}
		}
		if (event instanceof AttackEvent && flushes.get("Attack")) {
			resetPackets();
			nextDelay = attackFlush.getValue();
		}
		if (event instanceof ChangeSprintEvent && flushes.get("SprintChange")) {
			resetPackets();
			nextDelay = sprintFlush.getValue();
		}
		if (event instanceof PacketEvent packetEvent) {
			Packet<?> packet = packetEvent.getPacket();
			PackerDirection direction = packetEvent.getDirection();

			if (mc.isSingleplayer())
				return;

			if (packet instanceof C01PacketChatMessage
					|| packet instanceof C00PacketServerQuery
					|| packet instanceof C00PacketLoginStart) {
				return;
			}

			if (packet instanceof C08PacketPlayerBlockPlacement && flushes.get("BlockPlace")) {
				nextDelay = blockPlaceFlush.getValue();
			}

			// Ресет при получении урона
			if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId() && flushes.get("Velocity")) {
				resetPackets();
				nextDelay = velocityFlush.getValue();
			}

			if (mc.currentScreen != null && flushes.get("OpenedInv")) {
				resetPackets();
				nextDelay = openInvFlush.getValue();
			}

			if (mc.thePlayer.isUsingItem() && flushes.get("UsingItem")) {
				resetPackets();
				nextDelay = usingItemFlush.getValue();
			}

			if (packet instanceof S08PacketPlayerPosLook && flushes.get("Flag")) {
				resetPackets();
				nextDelay = flagFlush.getValue();
			}

			if (nextDelay > 0) {
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
			nextDelay -= (int) timer.reachedMS();
			timer.reset();

			if (nextDelay < 0) {
				nextDelay = 0;
			}

			handleStandAlone();

			if (posBuffer.isEmpty()) {
				return;
			}

			Vec3 vec = posBuffer.get(0).getFirst();
			animation3D.endX = vec.xCoord;
			animation3D.endY = vec.yCoord;
			animation3D.endZ = vec.zCoord;
			animation3D.update(20f);

			if (mc.gameSettings.thirdPersonView == 0 && onlyThirdPerson.isToggled()) {
				if (player != null) {
					mc.theWorld.removeEntityFromWorld(player.getEntityId());
					player = null;
				}
				return;
			}

			if (player == null) {
				player = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
				mc.theWorld.addEntityToWorld(player.getEntityId(), player);
			} else {
				player.setPositionAndRotation(
						animation3D.x,
						animation3D.y,
						animation3D.z,
						MathHelper.wrapDegree(mc.thePlayer.rotationYaw),
						mc.thePlayer.rotationPitch
				);
				player.rotationYawHead = mc.thePlayer.rotationYawHead;
				player.limbSwing = mc.thePlayer.limbSwing;
				player.prevLimbSwingAmount = mc.thePlayer.prevLimbSwingAmount;
				player.limbSwingAmount = mc.thePlayer.limbSwingAmount;
				player.swingProgressInt = mc.thePlayer.swingProgressInt;
				player.swingProgress = mc.thePlayer.swingProgress;
				player.renderYawOffset = mc.thePlayer.renderYawOffset;
			}
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
		packetBuffer.forEach(packet -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet.getFirst()));
		packetBuffer.clear();
		posBuffer.clear();
		if (player != null) {
			mc.theWorld.removeEntity(player);
			player = null;
		}
	}
}