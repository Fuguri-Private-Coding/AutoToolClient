package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.misc.ClientHandler;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Ping", category = Category.CONNECTION)
public class Ping extends Module {

	Vec3 serverPos, lServerPos;
	Rotation serverRotation;
	int outDelay, nextDelay;
	final StopWatch renderStopWatch;

	IntegerSetting minOutDelay = new IntegerSetting("MinOutDelay", this, 10, 1000, 450) {
		@Override
		public int getValue() {
			if (maxOutDelay.value < value) { value = maxOutDelay.value; }
			return super.getValue();
		}
	};

	IntegerSetting maxOutDelay = new IntegerSetting("MaxOutDelay", this, 10, 1000, 450) {
		@Override
		public int getValue() {
			if (minOutDelay.value > value) { value = minOutDelay.value; }
			return super.getValue();
		}
	};

	MultiBooleanSetting flushes = new MultiBooleanSetting("FlushConditions", this)
			.add("Attack")
			.add("SprintChange")
			.add("Teleport")
			.add("ToggledScaffold")
			.add("PlaceBlock")
			.add("OpenedInv")
			.add("Velocity")
			.add("WorldChange")
			.add("UsingItem")
			;

	IntegerSetting attackFlush = new IntegerSetting("AttackTime", this, () -> flushes.get("Attack"), 1, 20, 5);
	IntegerSetting sprintFlush = new IntegerSetting("SprintChangeTime", this, () -> flushes.get("SprintChange"), 1, 20, 5);
	IntegerSetting flagFlush = new IntegerSetting("TeleportTime", this, () -> flushes.get("Teleport"), 1, 20, 5);
	IntegerSetting velocityFlush = new IntegerSetting("VelocityTime", this, () -> flushes.get("Velocity"), 1, 20, 5);
	IntegerSetting openInvFlush = new IntegerSetting("OpenedInvTime", this, () -> flushes.get("OpenedInv"), 1, 20, 5);
	IntegerSetting usingItemFlush = new IntegerSetting("UsingItemTime", this, () -> flushes.get("UsingItem"), 1, 20, 5);
	IntegerSetting blockPlaceFlush = new IntegerSetting("BlockPlaceTime", this, () -> flushes.get("PlaceBlock"), 1, 20, 5);
	IntegerSetting worldChangeFlush = new IntegerSetting("WorldChangeTime", this, () -> flushes.get("WorldChange"), 1, 20, 5);

	MultiBooleanSetting render = new MultiBooleanSetting("Render", this)
			.add("HitBox")
			.add("Player")
			;

	public Ping() {
		renderStopWatch = new StopWatch();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		resetPackets();
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (mc.isSingleplayer())
			return;

		if (event instanceof PacketEvent packetEvent) {
			if (packetEvent.isCanceled())
				return;

			if (!mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ)))
				return;

			if (mc.thePlayer == null || mc.theWorld == null)
				return;

			if (mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 0)
				return;

			Packet packet = packetEvent.getPacket();

			if (packet instanceof C00PacketLoginStart
			|| packet instanceof C01PacketPing
			|| packet instanceof C01PacketChatMessage
			|| packet instanceof S01PacketPong
			|| packet instanceof S00PacketServerInfo
			|| packet instanceof C00PacketServerQuery
			|| packet instanceof C00Handshake) {
				return;
			}

			if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId() && flushes.get("Velocity")) {
				resetPackets();
				nextDelay = velocityFlush.getValue();
			}

			if (packet instanceof S08PacketPlayerPosLook && flushes.get("Teleport")) {
				resetPackets();
				nextDelay = flagFlush.getValue();
			}

			if (packet instanceof C08PacketPlayerBlockPlacement && flushes.get("PlaceBlock")) {
				resetPackets();
				nextDelay = blockPlaceFlush.getValue();
			}

			if (mc.currentScreen instanceof GuiContainer && flushes.get("OpenedInv")) {
				resetPackets();
				nextDelay = openInvFlush.getValue();
			}

			if (packetEvent.getDirection() == PacketDirection.OUTGOING) {
				ClientHandler.PacketHandler.clientPacketBuffer.add(new Doubles<>(packet, packetEvent.getSendTime()));
				packetEvent.setCanceled(true);
			}
		}

		if (nextDelay > 0) {
			resetPackets();
			if (event instanceof TickEvent) nextDelay--;
			return;
		}

		if (event instanceof RunGameLoopEvent) {
			ClientHandler.PacketHandler.clientPacketBuffer.forEach(p -> {
				if (System.currentTimeMillis() - p.getSecond() >= (long) outDelay) {
					mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.getFirst());
					ClientHandler.PacketHandler.clientPacketBuffer.remove(p);

					if (p.getFirst() instanceof C03PacketPlayer c03 && c03.isMoving()) {
						if (serverPos != null) {
							lServerPos = new Vec3(serverPos);
						}
						serverPos = c03.getPosVec();
						renderStopWatch.reset();
					}
				}
			});
		}

		if (event instanceof Render3DEvent) {
			if (serverPos != null && lServerPos != null) {
				if (mc.gameSettings.thirdPersonView == 0) return;

				double d1 = Math.min(renderStopWatch.reachedMS(), 50);
				d1 /= 50;

				double smoothX = lServerPos.xCoord + (serverPos.xCoord - lServerPos.xCoord) * d1 - mc.getRenderManager().viewerPosX;

				double smoothY = lServerPos.yCoord + (serverPos.yCoord - lServerPos.yCoord) * d1 - mc.getRenderManager().viewerPosY;

				double smoothZ = lServerPos.zCoord + (serverPos.zCoord - lServerPos.zCoord) * d1 - mc.getRenderManager().viewerPosZ;

				//if (nextDelay > 0) {
				//	smoothX = mc.thePlayer.posX;
				//	smoothY = mc.thePlayer.posY;
				//	smoothZ = mc.thePlayer.posZ;
				//}

				if (render.get("HitBox")) {
					RenderUtils.start3D();
					RenderUtils.renderHitBox(new AxisAlignedBB(
							smoothX - mc.thePlayer.width / 2,
							smoothY + 0,
							smoothZ - mc.thePlayer.width / 2,
							smoothX + mc.thePlayer.width / 2,
							smoothY + mc.thePlayer.height,
							smoothZ + mc.thePlayer.width / 2
					));
					RenderUtils.stop3D();
				}

				if (render.get("Player")) {
					mc.getRenderManager().doRenderEntity(
							mc.thePlayer,
							smoothX, smoothY, smoothZ,
							mc.thePlayer.rotationYawHead,
							mc.timer.renderPartialTicks,
							true
					);
				}
			}
		}
		if (mc.thePlayer.isUsingItem() && flushes.get("UsingItem")) {
			resetPackets();
			nextDelay = usingItemFlush.getValue();
		}
		if (event instanceof AttackEvent && flushes.get("Attack")) {
			resetPackets();
			nextDelay = attackFlush.getValue();
		}
		if (event instanceof ChangeSprintEvent && flushes.get("SprintChange")) {
			resetPackets();
			nextDelay = sprintFlush.getValue();
		}
		if (event instanceof WorldChangeEvent && flushes.get("WorldChange")) {
			resetPackets();
			nextDelay = worldChangeFlush.getValue();
		}
	}

	void resetPackets() {
		ClientHandler.PacketHandler.clientPacketBuffer.forEach(p -> {
			sendPacket(new Doubles<>(p.getFirst(), PacketDirection.OUTGOING));
			if (p.getFirst() instanceof C03PacketPlayer c03 && c03.isMoving()) {
				lServerPos = new Vec3(serverPos);
				serverPos = c03.getPosVec();
				renderStopWatch.reset();
			}
		});
		ClientHandler.PacketHandler.clientPacketBuffer.clear();

		outDelay = RandomUtils.nextInt(minOutDelay.getValue(), maxOutDelay.getValue());
	}

	void sendPacket(Doubles<Packet, PacketDirection> packet) {
		if (packet.getSecond() == PacketDirection.OUTGOING) {
			mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet.getFirst());
			if (packet.getFirst() instanceof C03PacketPlayer c03) {
				if (c03.isMoving()) {
					lServerPos = new Vec3(serverPos);
					serverPos = c03.getPosVec();
					renderStopWatch.reset();
				}
				if (c03.getRotating()) {
					serverRotation = new Rotation(c03.getYaw(), c03.getPitch());
				}
			}
		}
		if (packet.getSecond() == PacketDirection.INCOMING) {
			try {
				packet.getFirst().processPacket(mc.getNetHandler());
			} catch (RuntimeException e) {
				System.out.println(e.getMessage());
				throw new RuntimeException(e);
			}
		}
	}


	//				RenderUtils.renderHitBox(new AxisAlignedBB(
//						smoothX - mc.thePlayer.width / 2,
//						smoothY + 0,
//						smoothZ - mc.thePlayer.width / 2,
//						smoothX + mc.thePlayer.width / 2,
//						smoothY + mc.thePlayer.height,
//						smoothZ + mc.thePlayer.width / 2
//				));

	//	IntegerSetting delay = new IntegerSetting("Delay", this, 50, 1000, 500);
//
//	MultiBooleanSetting flushes = new MultiBooleanSetting("FlushConditions", this)
//			.add("Attack", false)
//			.add("SprintChange", true)
//			.add("Flag", true)
//			.add("Velocity", false)
//			.add("OpenedInv", true)
//			.add("UsingItem", true)
//			.add("BlockPlace", true)
//			.add("WorldChange", false);
//
//	IntegerSetting attackFlush = new IntegerSetting("AttackTime", this, () -> flushes.get("Attack"), 10, 1000, 20);
//	IntegerSetting sprintFlush = new IntegerSetting("SprintChangeTime", this, () -> flushes.get("SprintChange"), 10, 1000, 100);
//	IntegerSetting flagFlush = new IntegerSetting("FlagTime", this, () -> flushes.get("Flag"), 10, 1000, 200);
//	IntegerSetting velocityFlush = new IntegerSetting("VelocityTime", this, () -> flushes.get("Velocity"), 10, 1000, 500);
//	IntegerSetting openInvFlush = new IntegerSetting("OpenedInvTime", this, () -> flushes.get("OpenedInv"), 10, 1000, 50);
//	IntegerSetting usingItemFlush = new IntegerSetting("UsingItemTime", this, () -> flushes.get("UsingItem"), 10, 1000, 50);
//	IntegerSetting blockPlaceFlush = new IntegerSetting("BlockPlaceTime", this, () -> flushes.get("BlockPlace"), 10, 1000, 50);
//	IntegerSetting worldChangeFlush = new IntegerSetting("WorldChangeTime", this, () -> flushes.get("WorldChange"), 10, 1000, 50);
//
//	BooleanSetting onlyThirdPerson = new BooleanSetting("OnlyThirdPerson", this, true);
//
//	int nextDelay;
//	final StopWatch timer;
//	final Animation3D animation3D;
//	final public List<Doubles<Packet, Long>> packetBuffer;
//	ClientShader clientShader;
//	final List<Doubles<Vec3, Long>> posBuffer;
//	EntityOtherPlayerMP player;
//
//	static final long checkPerSecond = 200;
//	final StopWatch timer2;
//
//	public Ping() {
//		animation3D = new Animation3D();
//		packetBuffer = new CopyOnWriteArrayList<>();
//		posBuffer = new CopyOnWriteArrayList<>();
//		timer = new StopWatch();
//		timer2 = new StopWatch();
//	}
//
//	Thread thread;
//
//	@Override
//	public void onEnable() {
//		super.onEnable();
//	}
//
//	@Override
//	public void onDisable() {
//		resetPackets();
//		thread.stop();
//	}
//
//	@Override
//	public void onEvent(Event event) {
//		super.onEvent(event);
//		if (clientShader == null) {
//			clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
//			return;
//		}
//		if (event instanceof WorldChangeEvent) {
//			mc.theWorld.removeEntity(player);
//			player = null;
//			if (flushes.get("WorldChange")) {
//				resetPackets();
//				nextDelay = worldChangeFlush.getValue();
//			}
//		}
//		if (event instanceof AttackEvent && flushes.get("Attack")) {
//			resetPackets();
//			nextDelay = attackFlush.getValue();
//		}
//		if (event instanceof ChangeSprintEvent && flushes.get("SprintChange")) {
//			resetPackets();
//			nextDelay = sprintFlush.getValue();
//		}
//		if (event instanceof PacketEvent packetEvent) {
//			Packet<?> packet = packetEvent.getPacket();
//			PackerDirection direction = packetEvent.getDirection();
//
//			if (mc.isSingleplayer())
//				return;
//
//			if (packet instanceof C01PacketChatMessage
//					|| packet instanceof C00PacketServerQuery
//					|| packet instanceof C00PacketLoginStart) {
//				return;
//			}
//
//			if (packet instanceof C08PacketPlayerBlockPlacement && flushes.get("BlockPlace")) {
//				nextDelay = blockPlaceFlush.getValue();
//			}
//
//			// Ресет при получении урона
//			if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId() && flushes.get("Velocity")) {
//				resetPackets();
//				nextDelay = velocityFlush.getValue();
//			}
//
//			if (mc.currentScreen != null && flushes.get("OpenedInv")) {
//				resetPackets();
//				nextDelay = openInvFlush.getValue();
//			}
//
//			if (mc.thePlayer.isUsingItem() && flushes.get("UsingItem")) {
//				resetPackets();
//				nextDelay = usingItemFlush.getValue();
//			}
//
//			if (packet instanceof S08PacketPlayerPosLook && flushes.get("Flag")) {
//				resetPackets();
//				nextDelay = flagFlush.getValue();
//			}
//
//			if (nextDelay > 0) {
//				resetPackets();
//				return;
//			}
//
//			if (direction == PackerDirection.OUTGOING) {
//				packetEvent.setCanceled(true);
//				packetBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
//				if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
//					posBuffer.add(new Doubles<>(c03.getPosVec(), System.currentTimeMillis()));
//				}
//			}
//		}
//
//		if (event instanceof RunGameLoopEvent) {
//			nextDelay -= (int) timer.reachedMS();
//			timer.reset();
//
//			if (nextDelay < 0) {
//				nextDelay = 0;
//			}
//
//			handleStandAlone();
//
//			if (posBuffer.isEmpty()) {
//				return;
//			}
//
//			Vec3 vec = posBuffer.get(0).getFirst();
//
//			if (mc.gameSettings.thirdPersonView == 0 && onlyThirdPerson.isToggled() && player != null) {
//				mc.theWorld.removeEntityFromWorld(player.getEntityId());
//				return;
//			}
//
//			if (player == null) {
//				player = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
//				mc.theWorld.addEntityToWorld(player.getEntityId(), player);
//			} else {
//				player.setPositionAndRotation(
//						vec.xCoord,
//						vec.yCoord,
//						vec.zCoord,
//						MathHelper.wrapDegree(mc.thePlayer.rotationYaw),
//						mc.thePlayer.rotationPitch
//				);
//				player.rotationYawHead = mc.thePlayer.rotationYawHead;
//				player.limbSwing = mc.thePlayer.limbSwing;
//				player.prevLimbSwingAmount = mc.thePlayer.prevLimbSwingAmount;
//				player.limbSwingAmount = mc.thePlayer.limbSwingAmount;
//				player.swingProgressInt = mc.thePlayer.swingProgressInt;
//				player.swingProgress = mc.thePlayer.swingProgress;
//				player.renderYawOffset = mc.thePlayer.renderYawOffset;
//			}
//		}
//	}
//
//	void handleStandAlone() {
//		packetBuffer.forEach(packetLongDoubles -> {
//			if (System.currentTimeMillis() - packetLongDoubles.getSecond() >= delay.getValue()) {
//				mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetLongDoubles.getFirst());
//				packetBuffer.remove(packetLongDoubles);
//			}
//		});
//
//		posBuffer.removeIf(doubles -> System.currentTimeMillis() >= doubles.getSecond() + delay.getValue());
//	}
//
//	public void resetPackets() {
//		packetBuffer.forEach(packetLongDoubles -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetLongDoubles.getFirst()));
//		packetBuffer.clear();
//	}
}