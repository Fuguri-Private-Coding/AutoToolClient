package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.utils.time.TimedVar;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "BackTrack", category = Category.CONNECTION, description = "Задержкой пакетов увеличивает дальность удара.")
public class BackTrack extends Module {

    final DoubleSlider delay = new DoubleSlider("Delay", this, 0,5000,200,1);
    final BooleanSupplier constantRandomSupplier = () -> delay.minValue != delay.maxValue;
    final CheckBox constantRandomize = new CheckBox("ConstantDelayRandomize", this, constantRandomSupplier, true);

    final IntegerSetting delayBetweenTicks = new IntegerSetting("DelayBetweenBackTracks", this, 0, 20, 0) ;

    final DoubleSlider distance = new DoubleSlider("Distance", this, 0,12,12,0.1f);

    final CheckBox onlyWhenNeed = new CheckBox("OnlyWhenNeed", this, true);
    final IntegerSetting maxHurtTimeWhenWorking = new IntegerSetting("MaxHurtTimeWhenWorking",this, onlyWhenNeed::isToggled, 0, 10, 5);

    final CheckBox onlyKillAura = new CheckBox("OnlyKillAura", this, true);
    final CheckBox realTimeDamage = new CheckBox("RealTimeDamage", this, true);

    final CheckBox renderOnlyIfWorking = new CheckBox("RenderOnlyIfWorking", this, true);
    final Mode render = new Mode("Render", this)
            .addModes("Player", "HitBox", "Box", "OFF")
            .setMode("Player");

    final BooleanSupplier renderBox = () -> (render.getMode().equalsIgnoreCase("Box") || render.getMode().equalsIgnoreCase("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, () -> render.getMode().equalsIgnoreCase("HitBox"), 1f,5f,1f,0.1f);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    public final List<TimedVar<Packet>> packetBuffer = new CopyOnWriteArrayList<>();

    private EntityLivingBase target;
    private long delays = 90;

    private int delayBetweenBackTracks;

    public static boolean working;

    @Override
    public void onDisable() {
        working = false;
    }

    @Override
    public void onEvent(Event event) {
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
            if (delayBetweenBackTracks > 0 && !working) delayBetweenBackTracks--;
            if (constantRandomize.isToggled() && constantRandomSupplier.getAsBoolean()) delays = delay.getRandomizedIntValue();
        }

        if (event instanceof Render3DEvent) {
            EntityLivingBase needTarget = (onlyKillAura.isToggled() ? TargetStorage.getTarget() : TargetStorage.getTargetOrSelectedEntity());

            if (target != needTarget) {
                handle(true);
                target = needTarget;
            }

            if (delayBetweenBackTracks > 0) {
                handle(true);
                return;
            }

            handle(false);

            if (target != null) {
                Vec3 targetPos = new Vec3(target.nx, target.ny, target.nz);

                double offset = target.getCollisionBorderSize();

                AxisAlignedBB realBox = target.getEntityBoundingBox().offset(targetPos.xCoord - target.posX, targetPos.yCoord - target.posY, targetPos.zCoord - target.posZ).expand(offset, offset, offset);

                double distanceToReal = DistanceUtils.getDistance(realBox);
                double distanceToFake = DistanceUtils.getDistance(target);

                double threshold = 0;

                boolean improve = distanceToFake + threshold >= distanceToReal;
                boolean distance = distanceToReal > this.distance.getMaxValue() || distanceToFake > 3 || distanceToReal < this.distance.getMinValue();

                boolean need = onlyWhenNeed.isToggled() && target.hurtTime > maxHurtTimeWhenWorking.getValue();

                working = !improve && !distance && !need;

                if (improve || distance || need) {
                    handle(true);

                    delayBetweenBackTracks = delayBetweenTicks.getValue();
                    delays = delay.getRandomizedIntValue();

                    if (renderOnlyIfWorking.isToggled()) return;
                }

                targetPos = target.getRealPosition();
                Vec3 pos = targetPos;

                AxisAlignedBB bb = target.getEntityBoundingBox().offset(pos.xCoord - target.posX, pos.yCoord - target.posY, pos.zCoord - target.posZ);
                switch (render.getMode()) {
                    case "Player" -> {
                        if (glow.isToggled()) {
                            BloomUtils.addToDraw(() -> RenderUtils.renderPlayer(target, pos, target.rotationYawHead, mc.timer.renderPartialTicks, glowColor.getFadedColor()));
                        }
                        RenderUtils.renderPlayer(target, pos, target.rotationYawHead, mc.timer.renderPartialTicks);
                    }

                    case "Box" -> {
                        RenderUtils.start3D();
                        if (glow.isToggled()) {
                            BloomUtils.addToDraw(() -> RenderUtils.drawBoundingBox(bb, glowColor.getFadedColor()));
                        }
                        RenderUtils.drawBoundingBox(bb, color.getFadedColor());
                        RenderUtils.stop3D();
                    }

                    case "HitBox" -> {
                        RenderUtils.start3D();
                        if (glow.isToggled()) {
                            BloomUtils.addToDraw(() -> RenderUtils.drawHitBox(bb, glowColor.getFadedColor(), lineWidth.getValue()));
                        }
                        RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                        RenderUtils.stop3D();
                    }
                }
            }
        }
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

    @Override
    public String getSuffix() {
        return delays + " ms";
    }
}
