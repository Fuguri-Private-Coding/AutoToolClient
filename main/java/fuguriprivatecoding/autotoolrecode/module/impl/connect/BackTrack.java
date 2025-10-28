package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.TimedVar;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "BackTrack", category = Category.CONNECTION, description = "Абьюз интернета для увеличения дистанции удара.")
public class BackTrack extends Module {

    DoubleSlider delay = new DoubleSlider("Delay", this, 0,5000,200,1);
    DoubleSlider distance = new DoubleSlider("Distance", this, 0,12,12,0.1f);

    final IntegerSetting delayBetweenTicks = new IntegerSetting("DelayBetweenBackTracks", this, 0, 20, 0) ;
    final CheckBox onlyWhenNeed = new CheckBox("OnlyWhenNeed", this, true);
    final IntegerSetting maxHurtTimeWhenWorking = new IntegerSetting("MaxHurtTimeWhenWorking",this, onlyWhenNeed::isToggled, 0, 10, 5);

    CheckBox renderOnlyIfWorking = new CheckBox("RenderOnlyIfWorking", this, true);
    Mode render = new Mode("Render", this)
            .addModes("Player", "HitBox", "Box", "OFF")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (render.getMode().equalsIgnoreCase("Box") || render.getMode().equalsIgnoreCase("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, () -> render.getMode().equalsIgnoreCase("HitBox"), 1f,5f,1f,0.1f);

    CheckBox whileKillAura = new CheckBox("WhileKillAura", this, true);
    CheckBox realTimeDamage = new CheckBox("RealTimeDamage", this, true);

    public final List<TimedVar<Packet>> packetBuffer = new CopyOnWriteArrayList<>();

    private EntityLivingBase target;
    private long delays = 90;

    private int delayBetweenBackTracks;

    private Glow shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
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
            if (target != (whileKillAura.isToggled() ? Client.INST.getCombatManager().getTarget() : Client.INST.getCombatManager().getTargetOrSelectedEntity())) {
                handle(true);
                target = (whileKillAura.isToggled() ? Client.INST.getCombatManager().getTarget() : Client.INST.getCombatManager().getTargetOrSelectedEntity());
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
                boolean distance = distanceToReal > this.distance.getMaxValue() || distanceToFake > 3 || distanceToReal < this.distance.getMinValue();

                boolean need = onlyWhenNeed.isToggled() && target.hurtTime > maxHurtTimeWhenWorking.getValue();

                if (improve || distance || need) {
                    handle(true);

                    delayBetweenBackTracks = delayBetweenTicks.getValue();
                    delays = delay.getRandomizedIntValue();

                    if (renderOnlyIfWorking.isToggled()) return;
                }

                x = target.lrx + (target.rx - target.lrx) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                y = target.lry + (target.ry - target.lry) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                z = target.lrz + (target.rz - target.lrz) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                Vec3 pos = new Vec3(x,y,z);
                AxisAlignedBB bb = target.getEntityBoundingBox().offset(pos.xCoord - target.posX, pos.yCoord - target.posY, pos.zCoord - target.posZ);
                switch (render.getMode()) {
                    case "Player" -> {
                        if (shadows.module.get("BackTrack") && shadows.isToggled()) {
                            BloomUtils.addToDraw(() -> renderPlayer(pos, target, target.rotationYawHead, mc.timer.renderPartialTicks));
                        }
                        renderPlayer(pos, target, target.rotationYawHead, mc.timer.renderPartialTicks);
                    }

                    case "Box" -> {
                        if (shadows.module.get("BackTrack") && shadows.isToggled()) {
                            BloomUtils.addToDraw(() -> renderBox(bb, Color.white));
                        }
                        renderBox(bb, color.getFadedColor());
                    }

                    case "HitBox" -> {
                        if (shadows.module.get("BackTrack") && shadows.isToggled()) {
                            BloomUtils.addToDraw(() -> renderHitBox(bb, Color.white, lineWidth.getValue()));
                        }
                        renderHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                    }
                }
            }
        }
    }

    private void renderHitBox(AxisAlignedBB bb, Color color, float lineWidth) {
        RenderUtils.start3D();
        RenderUtils.drawHitBox(bb,color, lineWidth);
        RenderUtils.stop3D();
    }

    private void renderBox(AxisAlignedBB bb, Color color) {
        RenderUtils.start3D();
        RenderUtils.drawBoundingBox(bb,color);
        RenderUtils.stop3D();
    }

    private void renderPlayer(Vec3 pos, Entity target, float rotationYawHead, float partialTicks) {
        mc.getRenderManager().doRenderEntity(target, pos.xCoord, pos.yCoord, pos.zCoord, rotationYawHead, partialTicks, true);
        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
    }

    private void handle(boolean clear) {
        if (packetBuffer.isEmpty()) return;

        packetBuffer.removeIf(packet -> {
            if (System.currentTimeMillis() - packet.getTime() >= delays || clear) {
                try {
                    packet.getVar().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
                } catch (Exception ignored) {}
                return true;
            }
            return false;
        });
    }
}
