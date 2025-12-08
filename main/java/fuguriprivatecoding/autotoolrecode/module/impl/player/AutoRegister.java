package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.ChatMessageEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.security.SecureRandom;

@ModuleInfo(name = "AutoRegister", category = Category.PLAYER, description = "Автоматически регестрируется за вас.")
public class AutoRegister extends Module {

    final CheckBox randomPassword = new CheckBox("RandomPassword", this);

    final Mode password = new Mode("Password", this, () -> !randomPassword.isToggled())
        .addModes("12345678", "AutoHurakaniso")
        ;

    private final SecureRandom random = new SecureRandom();

    final StopWatch timer = new StopWatch();

    boolean active;

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChatMessageEvent e) {
            String message = e.getMessage().getFormattedText();

            if (message.contains("/reg")) {
                active = true;
                timer.reset();
            }
        }

        if (event instanceof TickEvent && active && timer.reachedMS(400L) && mc.thePlayer.ticksExisted > 20) {
            String password = randomPassword.isToggled() ? generate() : this.password.getMode();
            PacketUtils.sendPacket(new C01PacketChatMessage("/register " + password + " " + password));
            ClientUtils.chatLog("Successful registered.");
            active = false;
        }
    }


    private String generate() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        return random.ints(8 + random.nextInt(5), 0, CHARS.length())
            .mapToObj(CHARS::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}
