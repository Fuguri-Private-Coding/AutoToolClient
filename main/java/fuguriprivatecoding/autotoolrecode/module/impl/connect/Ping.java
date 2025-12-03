package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.ChangeSprintEvent;
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
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketWithTime;
import fuguriprivatecoding.autotoolrecode.utils.packet.VecWithTime;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
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

@ModuleInfo(name = "Ping", category = Category.CONNECTION, description = "Абьюз интернета для получение преимущевства.")
public class Ping extends Module {

    DoubleSlider delay = new DoubleSlider("Delay", this, 0,1000,200,1);

    Mode delayIncreaseType = new Mode("DelayIncreaseType", this)
            .addModes("Smooth", "Instant").setMode("Instant");

    DoubleSlider addingDelayPerTick = new DoubleSlider("AddingDelayPerTick", this, () -> delayIncreaseType.is("Smooth"), 0,100,20,1);

    CheckBox resetIfDistance = new CheckBox("ResetIfDistance", this);
    FloatSetting distance = new FloatSetting("Distance", this, resetIfDistance::isToggled, 2.5f, 6f,3f,0.01f);

    private final MultiMode actions = new MultiMode("ActionsToReset", this)
        .add("Attack", true)
        .add("Damage")
        .add("Velocity")
        .add("Flag")
        .add("UsingItem")
        .add("PlaceBlock")
        .add("ChangeSprint")
        .add("ClickWindow")
        .add("Scaffold")
        .add("OpenedGui");

    final IntegerSetting attackTimeCondition = new IntegerSetting("AttackTimeCondition", this, () -> actions.get("Attack"),0, 1000, 0);
    final IntegerSetting damageTimeCondition = new IntegerSetting("DamageTimeCondition", this, () -> actions.get("Damage"),0, 1000, 0);
    final IntegerSetting velocityTimeCondition = new IntegerSetting("VelocityTimeCondition", this, () -> actions.get("Velocity"),0, 1000, 0);
    final IntegerSetting flagTimeCondition = new IntegerSetting("FlagTimeCondition", this, () -> actions.get("Flag"),0, 1000, 0);
    final IntegerSetting usingItemTimeCondition = new IntegerSetting("UsingItemTimeCondition", this, () -> actions.get("UsingItem"),0, 1000, 0);
    final IntegerSetting placeBlockTimeCondition = new IntegerSetting("PlaceBlockTimeCondition", this, () -> actions.get("PlaceBlock"),0, 1000, 0);
    final IntegerSetting changeSprintTimeCondition = new IntegerSetting("ChangeSprintTimeCondition", this, () -> actions.get("ChangeSprint"),0, 1000, 0);
    final IntegerSetting clickWindowTimeCondition = new IntegerSetting("ClickWindowTimeCondition", this, () -> actions.get("ClickWindow"),0, 1000, 0);
    final IntegerSetting scaffoldTimeCondition = new IntegerSetting("ScaffoldTimeCondition", this, () -> actions.get("Scaffold"),0, 1000, 0);
    final IntegerSetting openedGuiTimeCondition = new IntegerSetting("OpenedGuiTimeCondition", this, () -> actions.get("OpenedGui"),0, 1000, 0);

    private final Mode renderModes = new Mode("RenderMode", this)
            .addModes("Player", "HitBox", "OFF")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (renderModes.getMode().equalsIgnoreCase("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f,5f,1f,0.1f);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    private int delays = 50;
    private long lastResetTime, delayBeforeNextLag;
    private final ConcurrentLinkedQueue<PacketWithTime> buffer = new ConcurrentLinkedQueue<>();
    private final List<VecWithTime> posBuffer = new CopyOnWriteArrayList<>();

    Vec3 lastPos, currentPos;

    @Override
    public void onDisable() {
        resetAllPackets();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.isIntegratedServerRunning()) return;
        long currentTime = System.currentTimeMillis();
        switch (event) {
            case ChangeSprintEvent _ when actions.get("ChangeSprint") -> reset(changeSprintTimeCondition.getValue());

            case WorldChangeEvent _ -> {
                reset(3000);
            }

            case PacketEvent e -> {
                if (currentTime - lastResetTime < delayBeforeNextLag) {
                    resetAllPackets();
                    break;
                }

                Packet packet = e.getPacket();

                switch (packet) {
                    case C01PacketChatMessage _ -> reset(50);
                    case C0EPacketClickWindow _ when actions.get("ClickWindow") -> reset(clickWindowTimeCondition.getValue());
                    case S08PacketPlayerPosLook _ when actions.get("Flag") -> reset(flagTimeCondition.getValue());
                    case C08PacketPlayerBlockPlacement _ when actions.get("PlaceBlock") -> reset(placeBlockTimeCondition.getValue());

                    case C02PacketUseEntity c02 when actions.get("Attack") && c02.getAction() == C02PacketUseEntity.Action.ATTACK ->
                        reset(attackTimeCondition.getValue());

                    case S12PacketEntityVelocity s12 when actions.get("Velocity") && s12.getId() == mc.thePlayer.getEntityId() ->
                        reset(velocityTimeCondition.getValue());

                    default -> {}
                }

                if (e.getDirection() == PacketDirection.OUTGOING) {
                    e.cancel();
                    buffer.add(new PacketWithTime(packet, currentTime));
                    if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
                        posBuffer.add(new VecWithTime(c03.getPosVec(), currentTime));
                    }
                }
            }

            case RunGameLoopEvent _ -> handlePackets();

            case TickEvent _ -> {
                if ((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) && actions.get("OpenedGui"))
                    reset(openedGuiTimeCondition.getValue());
                if (Modules.getModule(Scaffold.class).isToggled() && actions.get("Scaffold"))
                    reset(scaffoldTimeCondition.getValue());

                if (actions.get("Damage") && mc.thePlayer.hurtTime != 0) reset(damageTimeCondition.getValue());
                if (actions.get("UsingItem") && mc.thePlayer.isUsingItem()) reset(usingItemTimeCondition.getValue());

                if (currentTime - lastResetTime > delayBeforeNextLag &&
                    delayIncreaseType.getMode().equalsIgnoreCase("Smooth") &&
                    delays != delay.getMaxValue()) {
                    updateDelay(addingDelayPerTick.getRandomizedIntValue());
                }

                if (resetIfDistance.isToggled()) {
                    EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();

                    if (target != null && DistanceUtils.getDistance(target) <= distance.getValue()) {
                        reset(50);
                    }
                }

                lastPos = currentPos;
                currentPos = posBuffer.isEmpty() ? mc.thePlayer.getPositionVector() : posBuffer.getFirst().pos();
            }

            case Render3DEvent _ when !(mc.gameSettings.thirdPersonView == 0 || currentTime - lastResetTime < delayBeforeNextLag || lastPos == null || currentPos == null || renderModes.getMode().equalsIgnoreCase("OFF")) -> {
                EntityPlayerSP player = mc.thePlayer;

                double x = lastPos.xCoord + (currentPos.xCoord - lastPos.xCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                double y = lastPos.yCoord + (currentPos.yCoord - lastPos.yCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                double z = lastPos.zCoord + (currentPos.zCoord - lastPos.zCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                Vec3 pos = new Vec3(x, y, z);
                switch (renderModes.getMode()) {
                    case "HitBox" -> {
                        RenderUtils.start3D();
                        Vec3 diff = pos.subtract(player.getPositionVector());
                        AxisAlignedBB bb = player.getEntityBoundingBox().offset(diff);
                        if (glow.isToggled()) {
                            BloomUtils.addToDraw(() -> RenderUtils.drawHitBox(bb, glowColor.getFadedColor(), lineWidth.getValue()));
                        }
                        RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                        RenderUtils.stop3D();
                    }

                    case "Player" -> {
                        if (glow.isToggled()) {
                            BloomUtils.addToDraw(() -> RenderUtils.renderPlayer(player, pos, player.rotationYawHead, mc.timer.renderPartialTicks, glowColor.getFadedColor()));
                        }
                        RenderUtils.renderPlayer(player, pos, player.rotationYawHead, mc.timer.renderPartialTicks);
                    }
                }
            }
            default -> {}
        }
    }

    private void handlePackets() {
        buffer.removeIf(packetWithTime -> {
           if (System.currentTimeMillis() - packetWithTime.time() >= delays) {
               mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet());
               return true;
           }
           return false;
        });
        posBuffer.removeIf(pos -> System.currentTimeMillis() - pos.time() >= delays);
    }

    private void resetAllPackets() {
        buffer.forEach(packetWithTime -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet()));
        buffer.clear();
        posBuffer.clear();
    }

    private void reset(int time) {
        resetAllPackets();
        lastResetTime = System.currentTimeMillis();
        delayBeforeNextLag = time;
        if (delayIncreaseType.is("Smooth")) delays = 0;
        if (delayIncreaseType.is("Instant")) delays = delay.getRandomizedIntValue();
    }

    private void updateDelay(int addDelay) {
        delays += addDelay;
        delays = (int) Math.clamp(delays, delay.getMinValue(), delay.getMaxValue());
    }
}
