package me.hackclient.module.impl.client;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.profile.Profile;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;

@ModuleInfo(name = "IRC", category = Category.CLIENT)
public class IRCModule extends Module {

    private long myId = -1;
    private String currentInfo = "";
    public static HashMap<String, Profile> usersOnline = new HashMap<>();

    @Override
    public void onDisable() {
        if (myId != -1) Client.INSTANCE.getIrc().getServerChannel().deleteMessageById(myId).queue(_ -> {
            myId = -1;
        });
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {
            // Поиск других юзеров
            new Thread(() -> {
                usersOnline.clear();
                for (Message message : Client.INSTANCE.getIrc().getServerChannel().getIterableHistory().stream().toList()) {
                    String msg = message.getContentRaw();
                    String[] args = msg.split(" ");

                    String ign = args[0];
                    String clientName = args[1].replace("[", "").replace("]", "");
                    String role = args[2].replace("[", "").replace("]", "");

                    usersOnline.put(ign, new Profile(clientName, role));
                }
            }).start();

            // Отправка самого себя на сервер
            new Thread(() -> {
                if (myId == -1 || !currentInfo.equalsIgnoreCase(mc.getSession().getUsername() + Client.INSTANCE.getProfile())) {
                    if (myId != -1) {
                        ClientUtils.chatLog("удалил прошлое сообщение " + myId);
                        Client.INSTANCE.getIrc().getServerChannel().deleteMessageById(myId).queue();
                    }
                    ClientUtils.chatLog("Отправил сообщение " + mc.getSession().getUsername() + " " + Client.INSTANCE.getProfile());
                    Client.INSTANCE.getIrc().getServerChannel().sendMessage(
                            mc.getSession().getUsername() + " " + Client.INSTANCE.getProfile()
                    ).queue(sendMessage -> {
                        currentInfo = mc.getSession().getUsername() + Client.INSTANCE.getProfile();
                        myId = sendMessage.getIdLong();
                    });
                }
            }).start();
        }
    }
}
