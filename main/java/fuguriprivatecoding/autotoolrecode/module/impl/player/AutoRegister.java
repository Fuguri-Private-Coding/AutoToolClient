package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.ChatMessageEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.security.SecureRandom;

@ModuleInfo(name = "AutoRegister", category = Category.PLAYER, description = "Автоматически регестрируется за вас.")
public class AutoRegister extends Module {

    final CheckBox autoLogin = new CheckBox("AutoLogin", this, false);

    final FloatSetting delay = new FloatSetting("Delay", this, 0, 1000, 500, 50);

    final CheckBox randomPassword = new CheckBox("RandomPassword", this);
    final Mode password = new Mode("Password", this, () -> !randomPassword.isToggled())
        .addModes("12345678", "Qwerty1234", "AutoPensil")
        ;

    private final SecureRandom random = new SecureRandom();
    final StopWatch timer = new StopWatch();

    boolean active;
    String activeMessage = "";

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChatMessageEvent e) {
            String message = e.getMessage().getFormattedText();
            if (handleMessage(message)) setMessage(message);
        }

        if (mc.thePlayer.ticksExisted < 20) return;

        if (event instanceof TickEvent) {
            if (active && timer.reachedMS((long) delay.getValue())) {
                handle(activeMessage);
            }
        }
    }

    private boolean handleMessage(String message) {
        return message.contains("/reg") || (autoLogin.isToggled() && message.contains("/login"));
    }

    private void setMessage(String message) {
        activeMessage = message;
        active = true;
        timer.reset();
    }

    private void handle(String message) {
        String password = randomPassword.isToggled() ? generate() : this.password.getMode();

        if (message.contains("/reg")) {
            register(password);
        } else if (message.contains("/login") && autoLogin.isToggled()) {
            login(password);
        }

        active = false;
    }

    private void register(String password) {
        PacketUtils.sendPacket(new C01PacketChatMessage("/register " + password + " " + password));
        ClientUtils.chatLog("Successful register.");
    }

    private void login(String password) {
        PacketUtils.sendPacket(new C01PacketChatMessage("/login " + password));
        ClientUtils.chatLog("Successful login.");
    }

    private String generate() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        return random.ints(8 + random.nextInt(5), 0, CHARS.length())
            .mapToObj(CHARS::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}
