package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Test", category = Category.MISC, description = "тестовый модуль.")
public class Test extends Module {

//    private EntityLivingBase target;

    @Override
    public void onDisable() {
//        target = null;
//        mc.thePlayer.noClip = false;
//        for (C0FPacketConfirmTransaction c0f : c0fs) {
//            PacketUtils.sendPacket(c0f);
//        }
//        c0fs.clear();
//        perdet = false;


        for (Packet packet : incoming) {
            if (packet instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                double mx = s12.getMotionX() / 8000d;
                double my = s12.getMotionY() / 8000d;
                double mz = s12.getMotionZ() / 8000d;

//                System.out.printf("packet motion=%.2f %.2f %.2f%n", mx, my, mz);
            }

            try {
                packet.processPacket(mc.getNetHandler().getNetworkManager().packetListener);
            } catch (Exception ignored) {}
        }
//        System.out.printf("final motion=%.2f %.2f %.2f%n", mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        incoming.clear();
    }

    private boolean perdet;
    private List<C0FPacketConfirmTransaction> c0fs = new CopyOnWriteArrayList<>();
    private long lastHitTime;

    private final List<Packet> incoming = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {


        if (event instanceof PacketEvent e && e.getDirection() == PacketDirection.INCOMING) {
            e.cancel();
            incoming.add(e.getPacket());
            if (e.getPacket() instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                double mx = s12.getMotionX() / 8000d;
                double my = s12.getMotionY() / 8000d;
                double mz = s12.getMotionZ() / 8000d;

//                System.out.printf("running packet motion=%.2f %.2f %.2f%n", mx, my, mz);
            }
        }
    }

    public boolean shouldNoclip() {
        return false;
    }
}