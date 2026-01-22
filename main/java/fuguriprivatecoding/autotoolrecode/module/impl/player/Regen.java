package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Regen", category = Category.PLAYER, description = "Позволяет регенерировать быстрее.")
public class Regen extends Module {

    public final IntegerSetting health = new IntegerSetting("Health", this, 0, 20, 20);
    public final IntegerSetting hunger = new IntegerSetting("MinHunger", this, 0, 10, 10);
    public final IntegerSetting packets = new IntegerSetting("Packets", this, 0, 200, 200);
    public final CheckBox groundCheck = new CheckBox("GroundCheck", this, false);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.thePlayer.getFoodStats().getFoodLevel() > this.hunger.getValue() && mc.thePlayer.getHealth() < this.health.getValue()) {
                for (int i = 0; i < this.packets.getValue(); ++i) {
                    if (this.groundCheck.isToggled()) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    }
                }
            }
        }
    }
}
