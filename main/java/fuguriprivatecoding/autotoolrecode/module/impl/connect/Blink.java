package fuguriprivatecoding.autotoolrecode.module.impl.connect;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Blink", category = Category.CONNECTION, description = "Халяль Ломка-Интернета.")
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

    private int resetDelays;

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.isIntegratedServerRunning()) return;
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

        if (event instanceof Render3DEvent) {
            if (mc.gameSettings.thirdPersonView == 0 || positions.getFirst() == null || renderModes.getMode().equalsIgnoreCase("OFF")) {
                return;
            }

            Vec3 pos = positions.getFirst().subtract(RenderManager.getRenderPosition());

            switch (renderModes.getMode()) {
                case "HitBox" -> {
                    RenderUtils.start3D();
                    Vec3 diff = pos.subtract(mc.thePlayer.getPositionVector());
                    RenderUtils.drawHitBox(mc.thePlayer.getEntityBoundingBox().offset(diff), color.getFadedColor(), lineWidth.getValue());
                    RenderUtils.stop3D();
                }

                case "Player" -> RenderUtils.renderPlayer(mc.thePlayer, pos, mc.thePlayer.getRotationYawHead(), mc.timer.renderPartialTicks);
                default -> {}
            }
        }
    }

    private void reset() {
        buffer.forEach(PacketUtils::sendPacket);
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
