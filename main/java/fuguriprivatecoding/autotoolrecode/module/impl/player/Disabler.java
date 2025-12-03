package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketWithTime;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Disabler", category = Category.PLAYER, description = "Позволяет ослаблять/отключать античит.")
public class Disabler extends Module {

    Mode disableMode = new Mode("DisableMode", this)
        .addModes("Custom", "MatrixBalance")
        .setMode("MatrixBalance");

	CheckBox breaking = new CheckBox("Breaking", this, () -> disableMode.is("Custom"), false);

    final IntegerSetting delay = new IntegerSetting("Delay", this, () -> disableMode.is("MatrixBalance"), 100, 5000, 200);

    final CopyOnWriteArrayList<PacketWithTime> packets = new CopyOnWriteArrayList<>();

    @Override
    public void onDisable() {
        if (disableMode.is("MatrixBalance")) {
            for (PacketWithTime packet : packets) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet.packet());
            }
            packets.clear();
        }
    }

    @Override
	public void onEvent(Event event) {
        switch (disableMode.getMode()) {
            case "Custom" -> {
                if (event instanceof PacketEvent e) {
                    Packet packet = e.getPacket();

                    switch (packet) {
                        case C07PacketPlayerDigging c07 -> {
                            if ((c07.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
                                || c07.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) && breaking.isToggled()) {
                                e.cancel();
                            }
                        }
                        default -> {}
                    }
                }
            }

            case "MatrixBalance" -> {
                if (event instanceof RunGameLoopEvent) {
                    packets.removeIf(p -> {
                        if (System.currentTimeMillis() - p.time() >= delay.getValue() * 50L) {
                            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.packet());
                            return true;
                        }
                        return false;
                    });
                }
                if (event instanceof PacketEvent e) {
                    Packet packet = e.getPacket();

                    if (packet instanceof C0FPacketConfirmTransaction) {
                        e.cancel();
                        packets.add(new PacketWithTime(packet, System.currentTimeMillis()));
                    }
                    if (packet instanceof C03PacketPlayer && !packets.isEmpty()) {
                        if (mc.thePlayer.getBps(true) == 0D) {
                            e.cancel();
                        } else {
                            if (packets.size() > 1) {
                                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packets.getFirst().packet());
                                packets.removeFirst();
                            }
                        }
                    }
                }
                if (event instanceof Render2DEvent) {
                    ScaledResolution sc = new ScaledResolution(mc);
                    mc.fontRendererObj.drawString("Packet size: §9" + packets.size(), 25, sc.getScaledHeight() - 150, -1, false);
                }
            }
        }

	}
}
