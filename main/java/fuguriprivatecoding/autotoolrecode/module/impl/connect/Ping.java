package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketWithTime;
import fuguriprivatecoding.autotoolrecode.utils.packet.VecWithTime;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetFinder;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Ping", category = Category.CONNECTION, description = "Задерживает пакеты для получения преимущевства.")
public class Ping extends Module {

    final DoubleSlider delay = new DoubleSlider("Delay", this, 0,1000,200,1);

    final Mode delayIncreaseType = new Mode("DelayIncreaseType", this)
            .addModes("Smooth", "Instant").setMode("Smooth");

    final DoubleSlider addingDelayPerTick = new DoubleSlider("AddingDelayPerTick", this, () -> delayIncreaseType.is("Smooth"), 0,100,100,1);

    private final MultiMode actions = new MultiMode("ActionsToReset", this)
        .addModes("Attack", "Damage", "Velocity", "Flag", "UsingItem",
            "PlaceBlock", "ClickWindow", "Scaffold", "OpenedGui", "Distance", "ChatMessage");

    final FloatSetting distanceToReset = new FloatSetting("DistanceToReset", this, () -> actions.get("Distance"), 2.5f, 6f,3f,0.01f);

    final IntegerSetting chatMessageDelay = new IntegerSetting("ChatMessageDelay", this, () -> actions.get("ChatMessage"),0, 1000, 0);
    final IntegerSetting attackDelay = new IntegerSetting("AttackDelay", this, () -> actions.get("Attack"),0, 1000, 0);
    final IntegerSetting damageDelay = new IntegerSetting("DamageDelay", this, () -> actions.get("Damage"),0, 1000, 0);
    final IntegerSetting velocityDelay = new IntegerSetting("VelocityDelay", this, () -> actions.get("Velocity"),0, 1000, 0);
    final IntegerSetting flagDelay = new IntegerSetting("FlagDelay", this, () -> actions.get("Flag"),0, 1000, 0);
    final IntegerSetting usingItemDelay = new IntegerSetting("UsingItemDelay", this, () -> actions.get("UsingItem"),0, 1000, 0);
    final IntegerSetting placeBlockDelay = new IntegerSetting("PlaceBlockDelay", this, () -> actions.get("PlaceBlock"),0, 1000, 0);
    final IntegerSetting clickWindowDelay = new IntegerSetting("ClickWindowDelay", this, () -> actions.get("ClickWindow"),0, 1000, 0);
    final IntegerSetting scaffoldDelay = new IntegerSetting("ScaffoldDelay", this, () -> actions.get("Scaffold"),0, 1000, 0);
    final IntegerSetting openedGuiDelay = new IntegerSetting("OpenedGuiDelay", this, () -> actions.get("OpenedGui"),0, 1000, 0);

    private final Mode renderModes = new Mode("RenderMode", this)
            .addModes("Player", "HitBox", "OFF")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (renderModes.getMode().equalsIgnoreCase("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f,5f,1f,0.1f);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    private int currentDelay = 50;
    private long resetDelay;

    private static final ConcurrentLinkedQueue<PacketWithTime> buffer = new ConcurrentLinkedQueue<>();
    private final List<VecWithTime> posBuffer = new CopyOnWriteArrayList<>();

    Vec3 lastPos, currentPos;

    StopWatch delayTimer = new StopWatch();

    @Override
    public void onDisable() {
        resetAllPackets();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.isIntegratedServerRunning()) return;
        switch (event) {
            case WorldChangeEvent _ -> reset(3000);

            case PacketEvent e -> {
                if (!delayTimer.reachedMS(resetDelay)) {
                    resetAllPackets();
                    break;
                }

                Packet packet = e.getPacket();

                switch (packet) {
                    case C01PacketChatMessage _ when actions.get("ChatMessage") ->
                        reset(chatMessageDelay.getValue());

                    case C0EPacketClickWindow _ when actions.get("ClickWindow") ->
                        reset(clickWindowDelay.getValue());

                    case S08PacketPlayerPosLook _ when actions.get("Flag") ->
                        reset(flagDelay.getValue());

                    case C08PacketPlayerBlockPlacement _ when actions.get("PlaceBlock") ->
                        reset(placeBlockDelay.getValue());

                    case C02PacketUseEntity c02 when actions.get("Attack") && c02.getAction() == C02PacketUseEntity.Action.ATTACK ->
                        reset(attackDelay.getValue());

                    case S12PacketEntityVelocity s12 when actions.get("Velocity") && s12.getId() == mc.thePlayer.getEntityId() ->
                        reset(velocityDelay.getValue());

                    default -> {}
                }

                if (e.getDirection() == PacketDirection.OUTGOING) {
                    e.cancel();
                    buffer.add(new PacketWithTime(packet, System.currentTimeMillis()));
                    if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
                        posBuffer.add(new VecWithTime(c03.getPosVec(), System.currentTimeMillis()));
                    }
                }
            }

            case RunGameLoopEvent _ -> handlePackets();

            case TickEvent _ -> {
                if ((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) && actions.get("OpenedGui"))
                    reset(openedGuiDelay.getValue());

                if (actions.get("Scaffold") && Modules.getModule(Scaffold.class).isToggled())
                    reset(scaffoldDelay.getValue());

                if (actions.get("Damage") && mc.thePlayer.hurtTime != 0)
                    reset(damageDelay.getValue());

                if (actions.get("UsingItem") && mc.thePlayer.isUsingItem())
                    reset(usingItemDelay.getValue());

                handleSmoothDelay();

                if (actions.get("Distance")) {
                    EntityLivingBase target = TargetFinder.findTarget(6f, true, false, false);

                    if (target != null && DistanceUtils.getDistance(target) <= distanceToReset.getValue()) {
                        reset(50);
                    }
                }

                lastPos = currentPos;
                currentPos = posBuffer.isEmpty() ? mc.thePlayer.getPositionVector() : posBuffer.getFirst().pos();
            }

            case Render3DEvent _ when !(mc.gameSettings.thirdPersonView == 0 || !delayTimer.reachedMS(resetDelay) || lastPos == null || currentPos == null || renderModes.getMode().equalsIgnoreCase("OFF")) -> {
                Vec3 pos = RenderUtils.getAbsoluteSmoothPos(lastPos, currentPos).subtract(RenderManager.getRenderPosition());
                switch (renderModes.getMode()) {
                    case "HitBox" -> {
                        RenderUtils.start3D();
                        Vec3 diff = pos.subtract(mc.thePlayer.getPositionVector());
                        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(diff);
                        if (glow.isToggled()) {
                            BloomUtils.startWrite();
                            RenderUtils.drawHitBox(bb, glowColor.getFadedColor(), lineWidth.getValue());
                            BloomUtils.stopWrite();
                        }
                        RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                        RenderUtils.stop3D();
                    }

                    case "Player" -> {
                        if (glow.isToggled()) {
                            BloomUtils.startWrite();
                            RenderUtils.renderPlayer(mc.thePlayer, pos, mc.thePlayer.rotationYawHead, mc.timer.renderPartialTicks, glowColor.getFadedColor());
                            BloomUtils.stopWrite();
                        }
                        RenderUtils.renderPlayer(mc.thePlayer, pos, mc.thePlayer.rotationYawHead, mc.timer.renderPartialTicks);
                    }
                }
            }
            default -> {}
        }
    }

    private void handlePackets() {
        buffer.removeIf(packetWithTime -> {
            long packetTime = System.currentTimeMillis() - packetWithTime.time();
            if (packetTime >= currentDelay) {
                PacketUtils.sendPacket(packetWithTime.packet());
                return true;
            }

            return false;
        });
        posBuffer.removeIf(pos -> System.currentTimeMillis() - pos.time() >= currentDelay);
    }

    private void handleSmoothDelay() {
        if (delayTimer.reachedMS(resetDelay) &&
            delayIncreaseType.is("Smooth") &&
            currentDelay != delay.getMaxValue()) {
            updateDelay(addingDelayPerTick.getRandomizedIntValue());
        }
    }

    private void resetAllPackets() {
        buffer.forEach(packetWithTime -> PacketUtils.sendPacket(packetWithTime.packet()));
        buffer.clear();
        posBuffer.clear();
    }

    private void reset(int time) {
        resetAllPackets();
        delayTimer.reset();
        resetDelay = time;
        if (delayIncreaseType.is("Smooth")) currentDelay = 0;
        else currentDelay = delay.getRandomizedIntValue();
    }

    private void updateDelay(int addDelay) {
        currentDelay = (int) Math.clamp(currentDelay + addDelay, 0, delay.getMaxValue());
    }

    public static boolean isWorking() {
        return Modules.getModule(Ping.class).isToggled() && !buffer.isEmpty();
    }
}
