package me.hackclient.module.impl.client;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.UpdateIRCEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.utils.discord.IRC;
import me.hackclient.utils.profile.Profile;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;

@ModuleInfo(name = "IRC", category = Category.CLIENT)
public class IRCModule extends Module {

    CheckBox allVisibility = new CheckBox("AllVisibility", this, true);

    public static HashMap<String, Profile> usersOnline = new HashMap<>();
    public static volatile boolean running;

    @Override
    public void onEnable() {
        super.onEnable();
        running = true;
        Client.INST.getIrc().getIrcLogger().start();
        IRC.myID = -1;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        running = false;
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