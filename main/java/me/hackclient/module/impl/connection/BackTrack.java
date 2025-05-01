package me.hackclient.module.impl.connection;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.packet.TimedVar;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "BackTrack", category = Category.CONNECTION)
public class BackTrack extends Module {

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

    final IntegerSetting delayBetweenTicks = new IntegerSetting("DelayBetweenBackTracks", this, 0, 20, 0) ;

    BooleanSetting renderIfWorking = new BooleanSetting("RenderIfWorking", this, true);

    ModeSetting espMode = new ModeSetting("Render", this, "Player", new String[] { "Player", "Box" });
    ColorSetting color = new ColorSetting("Color", this, () -> espMode.getMode().equals("Box"), 1,1,1,1);

    BooleanSetting realTimeDamage = new BooleanSetting("RealTimeDamage", this, true);
    BooleanSetting debugDistance = new BooleanSetting("DebugDistance", this, true);

    private final List<TimedVar<Packet>> packetBuffer = new CopyOnWriteArrayList<>();

    private EntityLivingBase target;
    private long delay = 90;

    private int delayBetweenBackTracks;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (target != null && event instanceof TickEvent && debugDistance.isToggled()) {
            AxisAlignedBB realBox = target.getEntityBoundingBox().offset(target.nx - target.posX, target.ny - target.posY, target.nz - target.posZ).expand(
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize()
            );
            if (target.hurtTime == 10 && !packetBuffer.isEmpty() && DistanceUtils.getDistance(realBox) > 3) ClientUtils.chatLog("Distance: " + String.format("%.4f", DistanceUtils.getDistance(realBox)));
        }
        if (event instanceof PacketEvent e) {
            Packet packet = e.getPacket();
            if (target == null || e.isCanceled() || e.getDirection() != PacketDirection.INCOMING) return;

            if ((packet instanceof S06PacketUpdateHealth
                    || packet instanceof S02PacketChat
                    || packet instanceof S19PacketEntityStatus
                    || packet instanceof S29PacketSoundEffect)
                    && realTimeDamage.isToggled()) return;

            if (packet instanceof S13PacketDestroyEntities s13) {
                for (int entityID : s13.getEntityIDs()) {
                    if (entityID == target.getEntityId()) {
                        handle(true);
                        target = null;
                        return;
                    }
                }
            }

            e.setCanceled(true);
            packetBuffer.add(new TimedVar<>(packet));
        }

        if (event instanceof TickEvent) {
            if (delayBetweenBackTracks > 0) {
                delayBetweenBackTracks--;
            }
        }

        if (event instanceof Render3DEvent) {
            if (target != Client.INSTANCE.getCombatManager().getTarget()) {
                handle(true);
                target = Client.INSTANCE.getCombatManager().getTarget();
            }

            if (delayBetweenBackTracks > 0) {
                handle(true);
                return;
            }

            handle(false);

            if (target != null) {
                double x = target.nx;
                double y = target.ny;
                double z = target.nz;

                AxisAlignedBB realBox = target.getEntityBoundingBox().offset(x - target.posX, y - target.posY, z - target.posZ).expand(
                        target.getCollisionBorderSize(),
                        target.getCollisionBorderSize(),
                        target.getCollisionBorderSize()
                );

                double distanceToReal = DistanceUtils.getDistance(realBox);
                double distanceToFake = DistanceUtils.getDistance(target);

                double threshold = 0;

                boolean improve = distanceToFake + threshold >= distanceToReal;
                boolean distance = distanceToReal > maxDistance.getValue() || distanceToFake > 3 || distanceToReal < minDistance.getValue();

                if (improve || distance) {
                    handle(true);

                    delayBetweenBackTracks = delayBetweenTicks.getValue();
                    delay = RandomUtils.nextLong(minDelay.getValue(), maxDelay.getValue());

                    if (renderIfWorking.isToggled()) return;
                }

                x = target.lrx + (target.rx - target.lrx) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                y = target.lry + (target.ry - target.lry) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                z = target.lrz + (target.rz - target.lrz) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                switch (espMode.getMode()) {
                    case "Player" -> {
                        mc.entityRenderer.enableLightmap();
                        mc.getRenderManager().doRenderEntity(
                                target,
                                x, y, z,
                                target.getRotationYawHead(),
                                mc.timer.renderPartialTicks,
                                true
                        );

                        RenderHelper.disableStandardItemLighting();
                        mc.entityRenderer.disableLightmap();
                    }
                    case "Box" -> {
                        RenderUtils.start3D();
                        RenderUtils.drawBoundingBox(target.getEntityBoundingBox().offset(x - target.posX, y - target.posY, z - target.posZ), color.getColor());
                        GlStateManager.resetColor();
                        RenderUtils.stop3D();
                    }
                }
            }
        }
    }

    private void handle(boolean clear) {
        if (packetBuffer.isEmpty()) {
            return;
        }

        packetBuffer.removeIf(packet -> {
            if (System.currentTimeMillis() - packet.getTime() >= delay || clear) {
                try {
                    packet.getVar().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
                } catch (Exception ignored) {
                }
                return true;
            }
            return false;
        });
    }
}
