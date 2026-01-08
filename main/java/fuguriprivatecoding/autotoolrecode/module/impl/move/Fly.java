package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.BlockBBEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Fly", category = Category.MOVE, description = "Позволяет вам летать.")
public class Fly extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "Poral")
            .setMode("Vanilla");

    final FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 1f, 0.6f, 0.1f) {};

    List<Packet> packets = new CopyOnWriteArrayList<>();

    @Override
    public void onDisable() {
        if (mode.is("Poral")) {
            packets.forEach(PacketUtils::sendPacket);
            packets.clear();
        }

        if (mode.getMode().equalsIgnoreCase("Vanilla")) {
            mc.thePlayer.stopMotion();
            mc.thePlayer.capabilities.flySpeed = 0.05f;
            mc.thePlayer.capabilities.isFlying = false;
        }
    }

    @Override
    public void onEvent(Event event) {
        if (mode.getMode().equals("Vanilla")) {
            if (event instanceof UpdateEvent) {
                mc.thePlayer.capabilities.isFlying = true;
                mc.thePlayer.capabilities.flySpeed = speed.getValue();
            }
        }

        if (mode.is("Poral")) {
            if (event instanceof BlockBBEvent e) {
                if (!mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) return;

                AxisAlignedBB abb = new AxisAlignedBB(
                -2.0, -1.0, -2.0,
                2.0, 1.0, 2.0
                ).offset(
                    e.getBlockPos().getX(),
                    e.getBlockPos().getY(),
                    e.getBlockPos().getZ()
                );
                e.setBoundingBox(abb);
            }

            if (event instanceof PacketEvent e && !e.isCanceled() && Utils.isWorldLoaded()) {
                Packet packet = e.getPacket();

                switch (packet) {
                    case C00PacketKeepAlive _, C0FPacketConfirmTransaction _ -> {
                        packets.add(packet);
                        e.cancel();
                    }

                    default -> {}
                }

            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}