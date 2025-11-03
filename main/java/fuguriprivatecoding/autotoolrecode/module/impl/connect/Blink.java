package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Blink", category = Category.CONNECTION, description = "При включении вы зависаете от лица других, при выключении вас телепортирует туда куда вы пришли.")
public class Blink extends Module {

    private final CheckBox pulseBlink = new CheckBox("Pulse", this);

    DoubleSlider resetDelay = new DoubleSlider("ResetDelay", this, pulseBlink::isToggled, 0,5000,200,1);

    private final Mode renderModes = new Mode("RenderMode", this)
            .addModes("Player", "HitBox", "OFF")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (renderModes.getMode().equalsIgnoreCase("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f,5f,1f,0.1f);

    private final List<Packet> buffer = new CopyOnWriteArrayList<>();
    private final List<Vec3> positions = new CopyOnWriteArrayList<>();

    private final StopWatch timer = new StopWatch();

    private Vec3 currentPos, lastPos;
    private int resetDelays;

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.isIntegratedServerRunning()) return;
        if (event instanceof PacketEvent e && e.getDirection() == PacketDirection.OUTGOING && !e.isCanceled() && Utils.isWorldLoaded()) {
            Packet packet = e.getPacket();
            if (timer.reachedMS(resetDelays) && pulseBlink.isToggled()) {
                reset();
                return;
            }
            e.cancel();
            buffer.add(e.getPacket());
            if (packet instanceof C03PacketPlayer c03) {
                if (c03.isMoving()) {
                    positions.add(c03.getPosVec());
                }
            }
        }

        if (event instanceof TickEvent) {
            lastPos = currentPos;
            currentPos = positions.getFirst();
        }

        if (event instanceof Render3DEvent) {
            if (mc.gameSettings.thirdPersonView == 0 || currentPos == null || lastPos == null || renderModes.getMode().equalsIgnoreCase("OFF")) {
                return;
            }

            EntityPlayerSP player = mc.thePlayer;

            double x = lastPos.xCoord + (currentPos.xCoord - lastPos.xCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = lastPos.yCoord + (currentPos.yCoord - lastPos.yCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = lastPos.zCoord + (currentPos.zCoord - lastPos.zCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

            switch (renderModes.getMode()) {
                case "HitBox" -> {
                    RenderUtils.start3D();
                    Vec3 smoothPos = new Vec3(x,y,z);
                    Vec3 diff = smoothPos.subtract(player.getPositionVector());
                    RenderUtils.drawHitBox(player.getEntityBoundingBox().offset(diff), color.getFadedColor(), lineWidth.getValue());
                    RenderUtils.stop3D();
                }

                case "Player" -> {
                    mc.getRenderManager().doRenderEntity(
                            player,
                            x,y,z,
                            player.getRotationYawHead(),
                            mc.timer.renderPartialTicks,
                            true
                    );
                    mc.entityRenderer.disableLightmap();
                    RenderHelper.disableStandardItemLighting();
                }
                default -> {}
            }
        }
    }

    private void reset() {
        buffer.forEach(packet -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet));
        buffer.clear();
        resetDelays = resetDelay.getRandomizedIntValue();
        positions.clear();
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return String.valueOf(buffer.size());
    }
}
