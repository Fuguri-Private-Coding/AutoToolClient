package me.hackclient.utils.discord;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.Client;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.awt.*;

@Getter
@Setter
public class IRC extends ListenerAdapter {
    public MessageChannel chatChannel;
    public MessageChannel hwidChannel;

    public void init() {
        String token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GWZvER.shF_rSJG9yPypoRALEyRsmF-uEnUp5cPQxbyFw";

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGES
                )
                .addEventListeners(this)
                .build();

        try {
            jda.awaitReady();

            for (Guild guild : jda.getGuilds()) {
                for (MessageChannel channel : guild.getTextChannels()) {
                    if (channel.getName().equalsIgnoreCase("irc-chat")) {
                        setChatChannel(channel);
                    }
                    if (channel.getName().equalsIgnoreCase("login-log")) {
                        setHwidChannel(channel);
                    }
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw()
                .replaceAll("dev", "§4dev§f")
                .replaceAll(Client.INSTANCE.getProfile().getUsername(), "§b" + Client.INSTANCE.getProfile().getUsername() + "§f");;
        Client.INSTANCE.getConsole().history.add("§f[§2IRC§f] " + message);
    }

    public void sendIRCMessage(String text) {
        if (chatChannel == null) {
            return;
        }
        try {
            chatChannel.sendMessage(text).queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }

    public void sendPrivateMessage(User user, String text) {
        user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(text))
                .queue();
    }

    public void sendEmbedMessage(String title, String description) {
        if (chatChannel == null) {
            return;
        }
        chatChannel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(Color.GREEN)
                        .build()
        ).queue();
    }
}