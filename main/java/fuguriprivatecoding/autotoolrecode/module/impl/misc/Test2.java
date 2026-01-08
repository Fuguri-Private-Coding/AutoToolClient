package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// created by dicves_recode on 08.01.2026
@ModuleInfo(name = "Test2", category = Category.MISC)
public class Test2 extends Module {

    private IntegerSetting time = new IntegerSetting("Perdet", this, 0, 1000, 150);

    @Getter
    private boolean vonyat;
    private long lastHitTime;
    private List<Packet> packets = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketEvent e) {
            Packet packet = e.getPacket();

            if (packet instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId() && mc.thePlayer.onGround) {
//                e.cancel();
                vonyat = true;
                lastHitTime = System.currentTimeMillis();
            }

            if (vonyat && e.getDirection() == PacketDirection.OUTGOING) {
                e.cancel();
                packets.add(packet);
            }
        }

        if (event instanceof RunGameLoopEvent) {
            if (System.currentTimeMillis() - lastHitTime >= time.getValue() && vonyat) {
                vonyat = false;

                for (Packet packet : packets) {
                    PacketUtils.sendPacket(packet);
                }

                packets.clear();
            }
        }
    }
}
