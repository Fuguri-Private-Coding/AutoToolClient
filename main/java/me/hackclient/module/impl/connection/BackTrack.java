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
import me.hackclient.settings.impl.*;
import me.hackclient.utils.Utils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.packet.PacketUtils;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
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

    final StopWatch renderStopWatch;
    int delay;

    final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, 0, 500, 200) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };

    final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 0, 500, 200) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return super.getValue();
        }
    };

    final FloatSetting minDistance = new FloatSetting("MinDistance", this, 0.0f, 3.0f, 3.0f, 0.1f) {
        @Override
        public float getValue() {
            if (maxDistance.value < value) { value = maxDistance.value; }
            return super.getValue();
        }
    };

    final FloatSetting maxDistance = new FloatSetting("MaxDistance", this, 3.0f, 12.0f, 6.0f, 0.1f) {
        @Override
        public float getValue() {
            if (minDistance.value > value) { value = minDistance.value; }
            return super.getValue();
        }
    };

    final BooleanSetting showOnlyWorking = new BooleanSetting("ShowOnlyWhenWorking", this, true);
    final BooleanSetting showOnlyOnTarget = new BooleanSetting("ShowOnlyOnTarget", this, true);
    final BooleanSetting realTimeDamage = new BooleanSetting("RealTimeDamage", this,() -> mode.getMode().equals("Ping") , true);

    MultiBooleanSetting render = new MultiBooleanSetting("Render", this)
            .add("HitBox")
            .add("Player")
            ;

    public BackTrack() {
        new PositionResolver();
        renderStopWatch = new StopWatch();
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
            if (target == null || !Utils.isWorldLoaded()) { return; }

            Packet packet = packetEvent.getPacket();

            if (packet instanceof S06PacketUpdateHealth s06 && s06.getHealth() <= 0.0f) {
                resetPackets();
                return;
            }

            switch (mode.getMode()) {
                case "Classic" -> {
                    if (isValidPacket(packet) && packetEvent.getDirection() == PacketDirection.INCOMING) {
                        ClientHandler.PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                    }
                    packetEvent.setCanceled(true);
                }
                case "Ping" -> {
                    if (packetEvent.getDirection() != PacketDirection.INCOMING)
                        return;

                    if ((packet instanceof S0BPacketAnimation s0b && s0b.getAnimationType() == 1 || packet instanceof S29PacketSoundEffect || packet instanceof S06PacketUpdateHealth) && realTimeDamage.isToggled()) return;

                    // Отменяет все принимаемые пакеты, более легитно с сервер сайда
                    ClientHandler.PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                    packetEvent.setCanceled(true);
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
                    packetEvent.setCanceled(true);
                }
            }
        }
        if (event instanceof RunGameLoopEvent) {
            if (target == null) {
                resetPackets();
                return;
            }

            double distance = DistanceUtils.getDistanceToVec(new Vec3(target.realX, target.realY + target.getEyeHeight(), target.realZ));
            if (distance < minDistance.getValue() || distance > maxDistance.getValue() || distance < DistanceUtils.getDistanceToVec(target.getPositionEyes(1.0f))) {
                resetPackets();
                return;
            }

            ClientHandler.PacketHandler.serverPacketBuffer.forEach(p -> {
                if (System.currentTimeMillis() - p.getSecond() >= delay) {
                    PacketUtils.recievePacket(p.getFirst());
                    ClientHandler.PacketHandler.serverPacketBuffer.remove(p);
                }
            });
            ClientHandler.PacketHandler.clientPacketBuffer.forEach(p -> {
                if (System.currentTimeMillis() - p.getSecond() >= delay) {
                    PacketUtils.sendPacket(p.getFirst());
                    ClientHandler.PacketHandler.clientPacketBuffer.remove(p);
                }
            });
        }
        if (event instanceof Render3DEvent) {
            if (showOnlyWorking.isToggled() && (ClientHandler.PacketHandler.serverPacketBuffer.isEmpty() || target == null))
                return;

            if (target != null) {

                double d1 = Math.min(renderStopWatch.reachedMS(), 50);
                d1 /= 50D;

                double smoothX = target.lRealX + (target.realX - target.lRealX) * d1 - mc.getRenderManager().viewerPosX;
                double smoothY = target.lRealY + (target.realY - target.lRealY) * d1 - mc.getRenderManager().viewerPosY;
                double smoothZ = target.lRealZ + (target.realZ - target.lRealZ) * d1 - mc.getRenderManager().viewerPosZ;

                if (render.get("HitBox")) {
                    RenderUtils.start3D();
                    RenderUtils.renderHitBox(new AxisAlignedBB(
                            smoothX - target.width / 2,
                            smoothY + 0,
                            smoothZ - target.width / 2,
                            smoothX + target.width / 2,
                            smoothY + target.height,
                            smoothZ + target.width / 2
                    ));
                    RenderUtils.stop3D();
                }

                if (render.get("Player")) {
                    mc.entityRenderer.enableLightmap();
                    mc.getRenderManager().doRenderEntity(
                            target,
                            smoothX,
                            smoothY,
                            smoothZ,
                            target.rotationYawHead,
                            mc.timer.renderPartialTicks,
                            true
                    );
                    mc.entityRenderer.disableLightmap();
                }
            }
        }
    }

    void resetPackets() {
        switch (mode.getMode()) {
            case "Classic", "Ping" -> {
                ClientHandler.PacketHandler.resetServerPackets();
                renderStopWatch.reset();
            }
            case "LagBased" -> {
                ClientHandler.PacketHandler.resetClientPackets();
                ClientHandler.PacketHandler.resetServerPackets();
                renderStopWatch.reset();
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
                    entityLivingBase.lRealX = entityLivingBase.realX;
                    entityLivingBase.lRealY = entityLivingBase.realY;
                    entityLivingBase.lRealZ = entityLivingBase.realZ;
                    entityLivingBase.realX += (double) s14.getPositionX() / 32;
                    entityLivingBase.realY += (double) s14.getPositionY() / 32;
                    entityLivingBase.realZ += (double) s14.getPositionZ() / 32;
                }
                if (packet instanceof S18PacketEntityTeleport s18 && mc.theWorld.getEntityByID(s18.getEntityId()) instanceof EntityLivingBase entityLivingBase) {
                    entityLivingBase.lRealX = entityLivingBase.realX;
                    entityLivingBase.lRealY = entityLivingBase.realY;
                    entityLivingBase.lRealZ = entityLivingBase.realZ;
                    entityLivingBase.realX = (double) s18.getX() / 32;
                    entityLivingBase.realY = (double) s18.getY() / 32;
                    entityLivingBase.realZ = (double) s18.getZ() / 32;
                }
            }
        }
    }
}