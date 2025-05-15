package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.UpdateEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Regen", category = Category.PLAYER)
public class Regen extends Module {

    final IntegerSetting packets = new IntegerSetting("Packets", this, 0, 100, 10);

    final IntegerSetting health = new IntegerSetting("Heath", this, 0, 20, 18);
    final IntegerSetting food = new IntegerSetting("food", this, 0, 20, 18);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof UpdateEvent) {
            if (mc.thePlayer.getHealth() > health.getValue() || mc.thePlayer.getFoodStats().getFoodLevel() < food.getValue())
                return;

            if (mc.thePlayer.getBps(false) < 0.5) {
                mc.timer.timerSpeed = 0.3f;
                for (int i = 0; i < packets.getValue(); i++) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                }
            } else {
                mc.timer.timerSpeed = 1f;
            }
        }
    }
}
