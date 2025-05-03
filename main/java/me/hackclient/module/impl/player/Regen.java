package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
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
