package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

@ModuleInfo(name = "Debugger", category = Category.MISC)
public class Debugger extends Module {

    CheckBox checkTransactions = new CheckBox("Check Transactions", this, true);
    CheckBox limitTransactions = new CheckBox("Limit Check Transactions", this, () -> checkTransactions.isToggled(), true);

    IntegerSetting maxTicksExisted = new IntegerSetting("Max Ticks Existed", this, () -> checkTransactions.isToggled() && limitTransactions.isToggled(), 0, 100, 20);

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof PacketEvent e) {
            Packet packet = e.getPacket();

            if (packet instanceof C0FPacketConfirmTransaction c0F) {
                if (checkTransactions.isToggled()) {
                    if (mc.thePlayer.ticksExisted <= maxTicksExisted.getValue() && limitTransactions.isToggled()) {
                        ClientUtils.chatLog("Transaction uid: " + c0F.getUid());
                    } else if (!limitTransactions.isToggled()) {
                        ClientUtils.chatLog("Transaction uid: " + c0F.getUid());
                    }
                }
            }
        }
    }
}
