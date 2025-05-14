package me.hackclient.utils.discord;

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

public class IRC extends ListenerAdapter {
    private MessageChannel defaultChannel;
    private JDA jda;

    public void init() {
        String token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GWZvER.shF_rSJG9yPypoRALEyRsmF-uEnUp5cPQxbyFw";

        jda = JDABuilder.createDefault(token)
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
                    if (channel.getName().equalsIgnoreCase("ircchat")) {
                        setDefaultChannel(channel);
                        System.out.println("Установлен канал по умолчанию: #" + channel.getName());
                        return;
                    }
                }
            }

            System.err.println("Канал #ircchat не найден!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setDefaultChannel(MessageChannel channel) {
        this.defaultChannel = channel;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().replaceAll("dev", "§4dev§f").replaceAll(Client.INSTANCE.getProfile().getUsername(), "§b" + Client.INSTANCE.getProfile().getUsername() + "§f");;
        Client.INSTANCE.getConsole().history.add("§7[§2IRC§7] " + message);
    }

    public void sendMessage(String text) {
        if (defaultChannel == null) {
            System.err.println("Ошибка: канал по умолчанию не установлен!");
            return;
        }
        try {
            defaultChannel.sendMessage(text).queue();
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
        if (defaultChannel == null) {
            System.err.println("Ошибка: канал по умолчанию не установлен!");
            return;
        }
        defaultChannel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(Color.GREEN)
                        .build()
        ).queue();
    }
}