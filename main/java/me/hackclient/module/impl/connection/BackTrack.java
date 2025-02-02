package me.hackclient.module.impl.connection;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.misc.ClientHandler;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.Utils;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;


@ModuleInfo(name = "BackTrack", category = Category.CONNECTION)
public class BackTrack extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "Ping",
            new String[] {
                    "Classic",
                    "Ping",
                    "LagBased"
            }
    );

    int delay;

    final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, 0, 5000, 90);
    final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 0, 5000, 120);

    final FloatSetting minDistance = new FloatSetting("MinDistance", this, 0.0f, 3.0f, 3.0f, 0.1f);
    final FloatSetting maxDistance = new FloatSetting("MaxDistance", this, 3.0f, 12.0f, 6.0f, 0.1f);

    final BooleanSetting showOnlyWorking = new BooleanSetting("ShowOnlyWhenWorking", this, true);
    final BooleanSetting showOnlyOnTarget = new BooleanSetting("ShowOnlyOnTarget", this, true);

    public BackTrack() {
        new PositionResolver();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

        EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

        if (event instanceof TickEvent && target != null) {
            delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
        }
        if (event instanceof AttackEvent && mode.getMode().equals("LagBased")) {
            resetPackets();
        }
        if (event instanceof PacketEvent packetEvent) {
            if (target == null) { return; }

            Packet packet = packetEvent.getPacket();

            if (packet instanceof S06PacketUpdateHealth s06 && s06.getHealth() <= 0.0f) {
                resetPackets();
                return;
            }

            //if (packet instanceof C00Handshake || packet instanceof C00PacketServerQuery || packet instanceof S02PacketChat || packet instanceof S01PacketPong) {
            //    return;
            //}

            if (packet instanceof S13PacketDestroyEntities) {
                resetPackets();
                return;
            }


            switch (mode.getMode()) {
                case "Classic" -> {
                    if (isValidPacket(packet) && packetEvent.getDirection() == PacketDirection.INCOMING) {
                        ClientHandler.PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                    }
                }
                case "Ping" -> {
                    if (packetEvent.getDirection() != PacketDirection.INCOMING)
                        return;

                    // Отменяет все принимаемые пакеты, более легитно с сервер сайда
                    ClientHandler.PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                }
                case "LagBased" -> {
                    // Отменяет вообще все пакеты, самый легитный вариант
                    if (packetEvent.getDirection() == PacketDirection.OUTGOING) {
                        ClientHandler.PacketHandler.clientPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                    } else if (packetEvent.getDirection() == PacketDirection.INCOMING) {
                        if (packet instanceof S14PacketEntity
                                || packet instanceof S18PacketEntityTeleport
                                || packet instanceof S19PacketEntityHeadLook
                                || packet instanceof S0FPacketSpawnMob
                                || packet instanceof S08PacketPlayerPosLook) {
                            ClientHandler.PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                        }
                    }
                }
            }
            packetEvent.setCanceled(true);
        }
        if (event instanceof RunGameLoopEvent) {
            if (target == null) {
                resetPackets();
                return;
            }

            double distance = DistanceUtils.getDistanceToVec(new Vec3(target.realX / 32, target.realY / 32 + target.getEyeHeight(), target.realZ / 32));
            if (distance < minDistance.getValue() || distance > maxDistance.getValue() || distance < DistanceUtils.getDistanceToVec(target.getPositionEyes(1.0f))) {
                resetPackets();
                return;
            }

            ClientHandler.PacketHandler.serverPacketBuffer.forEach(p -> {
                if (System.currentTimeMillis() - p.getSecond() >= delay) {
                    try {
                        p.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
                        ClientHandler.PacketHandler.serverPacketBuffer.remove(p);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
            ClientHandler.PacketHandler.clientPacketBuffer.forEach(p -> {
                if (System.currentTimeMillis() - p.getSecond() >= delay) {
                    try {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.getFirst());
                        ClientHandler.PacketHandler.clientPacketBuffer.remove(p);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        }
        if (event instanceof Render3DEvent) {
            if (showOnlyWorking.isToggled() && target == null)
                return;

            if (showOnlyOnTarget.isToggled() && target != null) {
                RenderUtils.start3D();
                RenderUtils.renderHitBox(new AxisAlignedBB(
                        target.realX / 32 - target.width / 2,
                        target.realY / 32 + 0,
                        target.realZ / 32 - target.width / 2,
                        target.realX / 32 + target.width / 2,
                        target.realY / 32 + target.height,
                        target.realZ / 32 + target.width / 2
                ).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ));
                RenderUtils.stop3D();
            } else {
                for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                    RenderUtils.start3D();
                    RenderUtils.renderHitBox(new AxisAlignedBB(
                            playerEntity.realX / 32 - playerEntity.width / 2,
                            playerEntity.realY / 32 + 0,
                            playerEntity.realZ / 32 - playerEntity.width / 2,
                            playerEntity.realX / 32 + playerEntity.width / 2,
                            playerEntity.realY / 32 + playerEntity.height,
                            playerEntity.realZ / 32 + playerEntity.width / 2
                    ).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ));
                    RenderUtils.stop3D();
                }
            }
        }
    }

    void resetPackets() {
        switch (mode.getMode()) {
            case "Classic", "Ping" -> ClientHandler.PacketHandler.resetServerPackets();
            case "LagBased" -> {
                ClientHandler.PacketHandler.resetClientPackets();
                ClientHandler.PacketHandler.resetServerPackets();
            }
        }
    }

    boolean isValidPacket(Packet packet) {
        if (packet instanceof S03PacketTimeUpdate)
            return true;
        if (packet instanceof S00PacketKeepAlive)
            return true;
        if (packet instanceof S12PacketEntityVelocity)
            return true;
        if (packet instanceof S27PacketExplosion)
            return true;
        if (packet instanceof S32PacketConfirmTransaction) {
            return true;
        }

        return packet instanceof S14PacketEntity
                || packet instanceof S18PacketEntityTeleport
                || packet instanceof S19PacketEntityHeadLook
                || packet instanceof S0FPacketSpawnMob
                || packet instanceof S08PacketPlayerPosLook;
    }

    static class PositionResolver implements InstanceAccess, ConditionCallableObject {

        { callables.add(this); }

        @Override
        public boolean handleEvents() {
            return Utils.isWorldLoaded();
        }

        @Override
        public void onEvent(Event event) {
            if (event instanceof PacketEvent packetEvent) {
                Packet packet = packetEvent.getPacket();
                if (packet instanceof S14PacketEntity s14 && s14.getEntity(mc.theWorld) instanceof EntityLivingBase entityLivingBase) {
                    entityLivingBase.realX += s14.getPositionX();
                    entityLivingBase.realY += s14.getPositionY();
                    entityLivingBase.realZ += s14.getPositionZ();
                }
                if (packet instanceof S18PacketEntityTeleport s18 && mc.theWorld.getEntityByID(s18.getEntityId()) instanceof EntityLivingBase entityLivingBase) {
                    entityLivingBase.realX = s18.getX();
                    entityLivingBase.realY = s18.getY();
                    entityLivingBase.realZ = s18.getZ();
                }
            }
        }
    }
}