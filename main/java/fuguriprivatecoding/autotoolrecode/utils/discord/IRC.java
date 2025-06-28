package fuguriprivatecoding.autotoolrecode.utils.discord;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
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
    public MessageChannel loginChannel;
    public MessageChannel serverChannel;
    public MessageChannel hwidChannel;
    public MessageChannel onlineChannel;
    public MessageChannel changeLogChannel;
    public MessageChannel onlineConfigsChannel;
    public static long myID = -1;
    public static long myOnlineID = -1;

    public void init() {
        JDA jda;
        {
            {
                {
                    {
                        {
                            {
                                String token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GWZvER.shF_rSJG9yPypoRALEyRsmF-uEnUp5cPQxbyFw";

                                jda = JDABuilder.createDefault(token)
                                        .enableIntents(
                                                GatewayIntent.MESSAGE_CONTENT,
                                                GatewayIntent.GUILD_MESSAGES
                                        )
                                        .addEventListeners(this)
                                        .build();
                            }
                        }
                    }
                }
            }

            try {
                jda.awaitReady();

                for (Guild guild : jda.getGuilds()) {
                    for (MessageChannel channel : guild.getTextChannels()) {
                        if (channel.getName().equalsIgnoreCase("irc-chat")) setChatChannel(channel);
                        if (channel.getName().equalsIgnoreCase("login-log")) setLoginChannel(channel);
                        if (channel.getName().equalsIgnoreCase("server-log")) setServerChannel(channel);
                        if (channel.getName().equalsIgnoreCase("hwid-list")) setHwidChannel(channel);
                        if (channel.getName().equalsIgnoreCase("online-users")) setOnlineChannel(channel);
                        if (channel.getName().equalsIgnoreCase("change-log")) setChangeLogChannel(channel);
                        if (channel.getName().equalsIgnoreCase("online-configs")) setOnlineConfigsChannel(channel);
                    }
                }

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Client.INST.isStarting()) return;
        if (event.getChannel() == chatChannel) {
            String message = event.getMessage().getContentRaw();
            message = message.replaceAll(Client.INST.getProfile().toString(), Client.INST.getProfile().getColored());
            Client.INST.getConsole().history.add("§f[§2IRC§f] " + message);
        }
    }

    public void sendIRCMessage(String text) {
        chatChannel.sendMessage(text).queue();
    }

    public void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }

    public void sendEmbedMessage(String title, String description) {
        if (chatChannel == null) return;
        chatChannel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(Color.GREEN)
                        .build()
        ).queue();
    }
}