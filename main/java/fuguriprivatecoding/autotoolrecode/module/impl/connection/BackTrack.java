package fuguriprivatecoding.autotoolrecode.module.impl.connection;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.packet.TimedVar;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "BackTrack", category = Category.CONNECTION)
public class BackTrack extends Module {

    final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, 0, 5000, 200) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };
    final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 0, 5000, 200) {
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

    final CheckBox onlyWhenNeed = new CheckBox("OnlyWhenNeed", this, true);

    final IntegerSetting maxHurtTimeWhenWorking = new IntegerSetting("MaxHurtTimeWhenWorking",this, onlyWhenNeed::isToggled, 3, 8, 5);

    CheckBox renderOnlyIfWorking = new CheckBox("RenderOnlyIfWorking", this, true);

    Mode render = new Mode("Render", this)
            .addModes("Player", "Box", "OFF")
            .setMode("Player");

    final CheckBox fadeColor = new CheckBox("FadeColor", this, () -> render.getMode().equalsIgnoreCase("Box"));
    final ColorSetting color1 = new ColorSetting("Color1", this, () -> render.getMode().equalsIgnoreCase("Box"), 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, () -> render.getMode().equalsIgnoreCase("Box") && fadeColor.isToggled(), 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, () -> render.getMode().equalsIgnoreCase("Box") && fadeColor.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox realTimeDamage = new CheckBox("RealTimeDamage", this, true);
    CheckBox debugDistance = new CheckBox("DebugDistance", this, true);

    private final List<TimedVar<Packet>> packetBuffer = new CopyOnWriteArrayList<>();

    private EntityLivingBase target;
    private long delay = 90;

    private int delayBetweenBackTracks;

    @EventTarget
    public void onEvent(Event event) {
        if (target != null && event instanceof AttackEvent && debugDistance.isToggled()) {
            AxisAlignedBB realBox = target.getEntityBoundingBox().offset(target.nx - target.posX, target.ny - target.posY, target.nz - target.posZ).expand(
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize()
            );
            if (!packetBuffer.isEmpty() && DistanceUtils.getDistance(realBox) > 3) ClientUtils.chatLog("Distance: " + String.format("%.4f", DistanceUtils.getDistance(realBox)));
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
            if (delayBetweenBackTracks > 0) delayBetweenBackTracks--;
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
                boolean distance = distanceToReal > maxDistance.getValue() || distanceToFake > 3 || distanceToReal < minDistance.getValue();

                boolean need = onlyWhenNeed.isToggled() && target.hurtTime > maxHurtTimeWhenWorking.getValue();

                if (improve || distance || need) {
                    handle(true);

                    delayBetweenBackTracks = delayBetweenTicks.getValue();
                    delay = RandomUtils.nextLong(minDelay.getValue(), maxDelay.getValue());

                    if (renderOnlyIfWorking.isToggled()) return;
                }

                x = target.lrx + (target.rx - target.lrx) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                y = target.lry + (target.ry - target.lry) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                z = target.lrz + (target.rz - target.lrz) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                switch (render.getMode()) {
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
                        Color fadeColor;
                        if (this.fadeColor.isToggled()) {
                            fadeColor = ColorUtils.mixColors(color1.getColor(), color2.getColor(), fadeSpeed.getValue());
                        } else {
                            fadeColor = color1.getColor();
                        }

                        RenderUtils.start3D();
                        RenderUtils.drawBoundingBox(target.getEntityBoundingBox().offset(x - target.posX, y - target.posY, z - target.posZ), fadeColor);
                        GlStateManager.resetColor();
                        RenderUtils.stop3D();
                    }
                }
            }
        }
    }

    private void handle(boolean clear) {
        if (packetBuffer.isEmpty()) return;

        packetBuffer.removeIf(packet -> {
            if (System.currentTimeMillis() - packet.getTime() >= delay || clear) {
                try {
                    packet.getVar().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
                } catch (Exception ignored) {}
                return true;
            }
            return false;
        });
    }
}
