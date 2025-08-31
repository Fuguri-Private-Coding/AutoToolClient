package fuguriprivatecoding.autotoolrecode.module.impl.connection;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Ping", category = Category.CONNECTION, description = "Абьюз интернета для получение преимущевства.")
public class Ping extends Module {

    private final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, 50, 1000, 400) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };
    private final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 50, 1000, 400) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return super.getValue();
        }
    };

    Mode delayIncreaseType = new Mode("DelayIncreaseType", this)
            .addModes("Smooth", "Instant").setMode("Instant");

    BooleanSupplier smoothVisible = () -> delayIncreaseType.getMode().equalsIgnoreCase("Smooth");

    IntegerSetting minAddingDelayPerTick = new IntegerSetting("MinAddingDelayPerTick", this, smoothVisible, 1,1000,100) {
        @Override
        public int getValue() {
            if (maxAddingDelayPerTick.value < value) { value = maxAddingDelayPerTick.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxAddingDelayPerTick = new IntegerSetting("MaxAddingDelayPerTick", this, smoothVisible, 1,1000,100) {
        @Override
        public int getValue() {
            if (minAddingDelayPerTick.value > value) { value = minAddingDelayPerTick.value; }
            return super.getValue();
        }
    };

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

    private int delay = 50;
    private long lastResetTime, delayBeforeNextLag;
    private final ConcurrentLinkedQueue<PacketWithTime> buffer = new ConcurrentLinkedQueue<>();
    private final List<VecWithTime> posBuffer = new CopyOnWriteArrayList<>();

    Vec3 lastPos, currentPos;

    Color fadeColor;

    Glow shadows;

    @Override
    public void onDisable() {
        resetAllPackets();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.isIntegratedServerRunning()) return;
        long currentTime = System.currentTimeMillis();
        switch (event) {
            case ChangeSprintEvent _ -> {
                if (actions.get("ChangeSprint")) reset(changeSprintTimeCondition.getValue());
            }

            case PacketEvent e -> {
                if (currentTime - lastResetTime < delayBeforeNextLag) {
                    resetAllPackets();
                    break;
                }

                Packet packet = e.getPacket();

                switch (packet) {
                    case C01PacketChatMessage _ -> {
                        reset(0);
                        return;
                    }

                    case C02PacketUseEntity handlingPacket -> {
                        if (actions.get("Attack") && handlingPacket.getAction() == C02PacketUseEntity.Action.ATTACK) {
                            reset(attackTimeCondition.getValue());
                            return;
                        }
                    }

                    case C0EPacketClickWindow _ -> {
                        if (actions.get("ClickWindow")) {
                            reset(clickWindowTimeCondition.getValue());
                            return;
                        }
                    }

                    case S12PacketEntityVelocity handlingPacket -> {
                        if (actions.get("Velocity") && handlingPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                            reset(velocityTimeCondition.getValue());
                            return;
                        }
                    }

                    case S08PacketPlayerPosLook _ -> {
                        if (actions.get("Flag")) {
                            reset(flagTimeCondition.getValue());
                            return;
                        }
                    }

                    case C08PacketPlayerBlockPlacement _ -> {
                        if (actions.get("PlaceBlock")) {
                            reset(placeBlockTimeCondition.getValue());
                            return;
                        }
                    }

                    default -> {}
                }

                if (e.getDirection() == PacketDirection.OUTGOING) {
                    e.cancel();
                    buffer.add(new PacketWithTime(packet, currentTime));
                    if (packet instanceof C03PacketPlayer c03) {
                        if (c03.isMoving()) {
                            posBuffer.add(new VecWithTime(c03.getPosVec(), currentTime));
                        }
                    }
                }
            }
            case RunGameLoopEvent _ -> handlePackets();
            case TickEvent _ -> {
                if ((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) && actions.get("OpenedGui"))
                    reset(openedGuiTimeCondition.getValue());
                if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled() && actions.get("Scaffold"))
                    reset(scaffoldTimeCondition.getValue());

                if (actions.get("Damage") && mc.thePlayer.hurtTime != 0) reset(damageTimeCondition.getValue());
                if (actions.get("UsingItem") && mc.thePlayer.isUsingItem()) reset(usingItemTimeCondition.getValue());

                if (currentTime - lastResetTime > delayBeforeNextLag && delayIncreaseType.getMode().equalsIgnoreCase("Smooth") && delay != maxDelay.getValue()) updateDelay(RandomUtils.nextInt(minAddingDelayPerTick.getValue(), maxAddingDelayPerTick.getValue()));

                lastPos = currentPos;
                if (posBuffer.isEmpty()) {
                    currentPos = mc.thePlayer.getPositionVector();
                } else {
                    currentPos = posBuffer.getFirst().pos;
                }
            }
            case Render3DEvent _ -> {
                if (mc.gameSettings.thirdPersonView == 0 || lastPos == null || currentPos == null || renderModes.getMode().equalsIgnoreCase("OFF")) {
                    break;
                }

                EntityPlayerSP player = mc.thePlayer;

                double x = lastPos.xCoord + (currentPos.xCoord - lastPos.xCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                double y = lastPos.yCoord + (currentPos.yCoord - lastPos.yCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                double z = lastPos.zCoord + (currentPos.zCoord - lastPos.zCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                if (!renderModes.getMode().equalsIgnoreCase("Player")) updateColors();

                Vec3 pos = new Vec3(x, y, z);
                switch (renderModes.getMode()) {
                    case "HitBox" -> {
                        Vec3 diff = pos.subtract(player.getPositionVector());
                        AxisAlignedBB bb = player.getEntityBoundingBox().offset(diff);
                        if (shadows.module.get("Ping") && shadows.isToggled()) {
                            BloomUtils.addToDraw(() -> renderHitBox(bb, Color.white,lineWidth.getValue()));
                        }
                        renderHitBox(bb, fadeColor,lineWidth.getValue());
                    }

                    case "Player" -> {
                        if (shadows.module.get("Ping") && shadows.isToggled()) {
                            BloomUtils.addToDraw(() -> renderPlayer(player,pos,player.rotationYawHead,mc.timer.renderPartialTicks));
                        }
                        renderPlayer(player,pos,player.rotationYawHead,mc.timer.renderPartialTicks);
                    }
                }
            }
            default -> {}
        }
    }

    private void renderPlayer(Entity target, Vec3 pos, float rotationYawHead, float partialTicks) {
        mc.getRenderManager().doRenderEntity(target, pos.xCoord, pos.yCoord, pos.zCoord, rotationYawHead, partialTicks, true);
        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
    }

    private void renderHitBox(AxisAlignedBB bb, Color color, float lineWidth) {
        RenderUtils.start3D();
        RenderUtils.drawHitBox(bb, color, lineWidth);
        RenderUtils.stop3D();
    }

    private void updateColors() {
        fadeColor = color.isFade() ?
                ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                : color.getColor();
    }

    private void handlePackets() {
        buffer.removeIf(packetWithTime -> {
           if (System.currentTimeMillis() - packetWithTime.time() >= delay) {
               mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet());
               return true;
           }
           return false;
        });
        posBuffer.removeIf(pos -> System.currentTimeMillis() - pos.time() >= delay);
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
        if (delayIncreaseType.getMode().equalsIgnoreCase("Smooth")) delay = 0;
        if (delayIncreaseType.getMode().equalsIgnoreCase("Instant")) delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
    }

    private void updateDelay(int addDelay) {
        delay += addDelay;
        if (delay > maxDelay.getValue()) {
            delay = maxDelay.getValue();
        }
    }

    private record PacketWithTime(Packet packet, long time) {}
    private record VecWithTime(Vec3 pos, long time) {}
}
