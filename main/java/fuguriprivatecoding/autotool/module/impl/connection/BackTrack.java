package fuguriprivatecoding.autotool.module.impl.connection;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.PacketDirection;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.event.events.Render3DEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.*;
import fuguriprivatecoding.autotool.settings.impl.*;
import fuguriprivatecoding.autotool.utils.client.ClientUtils;
import fuguriprivatecoding.autotool.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotool.utils.math.RandomUtils;
import fuguriprivatecoding.autotool.utils.packet.TimedVar;
import fuguriprivatecoding.autotool.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
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

    final FloatSetting startDistance = new FloatSetting("StartDistance", this, 0.0f, 3.0f, 3.0f, 0.1f) {
        @Override
        public float getValue() {
            if (maxDistance.value < value) { value = maxDistance.value; }
            return super.getValue();
        }
    };
    final FloatSetting maxDistance = new FloatSetting("MaxDistance", this, 3.0f, 12.0f, 6.0f, 0.1f) {
        @Override
        public float getValue() {
            if (startDistance.value > value) { value = startDistance.value; }
            return super.getValue();
        }
    };

    final IntegerSetting delayBetweenTicks = new IntegerSetting("DelayBetweenBackTracks", this, 0, 20, 0) ;

    CheckBox renderOnlyIfWorking = new CheckBox("RenderOnlyIfWorking", this, true);

    Mode espMode = new Mode("Render", this)
            .addModes("Player", "Box", "OFF")
            .setMode("Player");

    ColorSetting color = new ColorSetting("Color", this, () -> espMode.getMode().equals("Box"), 1,1,1,1);

    CheckBox realTimeDamage = new CheckBox("RealTimeDamage", this, true);
    CheckBox debugDistance = new CheckBox("DebugDistance", this, true);

    private final List<TimedVar<Packet>> packetBuffer = new CopyOnWriteArrayList<>();

    private EntityLivingBase target;
    private long delay = 90;

    private int delayBetweenBackTracks;

    @EventTarget
    public void onEvent(Event event) {
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
            if (target != Client.INST.getCombatManager().getTarget()) {
                handle(true);
                target = Client.INST.getCombatManager().getTarget();
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
                boolean distance = distanceToReal > maxDistance.getValue() || distanceToFake > 3 || distanceToReal < startDistance.getValue();

                if (improve || distance) {
                    handle(true);

                    delayBetweenBackTracks = delayBetweenTicks.getValue();
                    delay = RandomUtils.nextLong(minDelay.getValue(), maxDelay.getValue());

                    if (renderOnlyIfWorking.isToggled()) return;
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
