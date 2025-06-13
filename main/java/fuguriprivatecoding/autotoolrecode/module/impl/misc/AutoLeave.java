package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.KeyBind;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "AutoLeave", category = Category.MISC)
public class AutoLeave extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Health", "HotKey")
            .setMode("HotKey")
            ;

    IntegerSetting health = new IntegerSetting("Health", this, () -> mode.getMode().equalsIgnoreCase("Health"), 1,20,3);

    KeyBind key = new KeyBind("Key", this, () -> mode.getMode().equalsIgnoreCase("HotKey"), Keyboard.KEY_M);

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "HotKey" -> {
                if (event instanceof KeyEvent keyEvent && keyEvent.getKey() == key.getKey()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/hub"));
                }
            }
            case "Health" -> {
                if (event instanceof TickEvent && mc.thePlayer.getHealth() <= health.getValue()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/hub"));
                }
            }
        }

    }
}