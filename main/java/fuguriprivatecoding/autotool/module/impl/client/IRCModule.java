package fuguriprivatecoding.autotool.module.impl.client;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.UpdateIRCEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.utils.discord.IRC;
import fuguriprivatecoding.autotool.utils.profile.Profile;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;

@ModuleInfo(name = "IRC", category = Category.CLIENT)
public class IRCModule extends Module {

    CheckBox allVisibility = new CheckBox("AllVisibility", this, true);

    public static HashMap<String, Profile> usersOnline = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        IRC.myID = -1;
        Client.INST.getIrc().getIrcLogger().start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Client.INST.getIrc().getIrcLogger().interrupt();
        IRC.myID = -1;
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof UpdateIRCEvent) {
            if (allVisibility.isToggled()) sendMyMessage();
            new Thread(() -> {
                usersOnline.clear();
                for (Message message : Client.INST.getIrc().getServerChannel().getIterableHistory().stream().toList()) {
                    String msg = message.getContentRaw();
                    String[] args = msg.split(" ");

                    String ign = args[0];
                    String clientName = args[1].replace("[", "").replace("]", "");
                    String role = args[2].replace("[", "").replace("]", "");

                    usersOnline.put(ign, new Profile(clientName, role));
                }
            }).start();
        }
    }

    public void sendMyMessage() {
        Client.INST.getIrc().getServerChannel().sendMessage(
                mc.getSession().getUsername() + " " + Client.INST.getProfile()
        ).queue(sendMessage -> IRC.myID = sendMessage.getIdLong());
    }
}