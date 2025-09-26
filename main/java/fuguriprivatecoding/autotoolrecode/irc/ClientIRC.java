package fuguriprivatecoding.autotoolrecode.irc;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class ClientIRC extends ListenerAdapter {

    @Getter @Setter
    public MessageChannel chatChannel, loginChannel, serverChannel, keyChannel,
        onlineChannel, changeLogChannel, onlineConfigsChannel, clientVersionChannel,
        clientCapesChannel, fontsChannel;

    @Getter @Setter
    public static long MESSAGE_ID = -1, ONLINE_MESSAGE_ID = -1;

    String token;

    JDA jda;

    public void init() {
        token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GGhvgp.juE97JuncYJRgH-Rzca0OV2a8ieMd2g6XzV1IA";

        try {
            jda = JDABuilder.createDefault(token)
                .enableIntents(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_MODERATION,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_WEBHOOKS,
                    GatewayIntent.SCHEDULED_EVENTS
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(this)
                .build();
        } catch (Exception e) {
            System.out.println("Failed setup intents.");
            System.exit(-1);
        }

        try {
            jda.awaitReady();

            for (Guild guild : jda.getGuilds()) {
                for (MessageChannel channel : guild.getTextChannels()) {
                    switch (channel.getName()) {
                        case "hwid-list" -> setKeyChannel(channel);
                        case "online-users" -> setOnlineChannel(channel);
                        case "change-log" -> setChangeLogChannel(channel);
                        case "online-configs" -> setOnlineConfigsChannel(channel);
                        case "client-version" -> setClientVersionChannel(channel);
                        case "client-capes" -> setClientCapesChannel(channel);
                        case "login-log" -> setLoginChannel(channel);
                        case "irc-chat" -> setChatChannel(channel);
                        case "server-log" -> setServerChannel(channel);
                        case "fonts" -> setFontsChannel(channel);
                    }
                }
            }

        } catch (InterruptedException e) {
            System.out.println("Failed connect to server.");
            System.exit(-1);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Client.INST.isStarting()) return;
        if (event.getChannel() == chatChannel) {
            String message = event.getMessage().getContentRaw();
            message = message.replaceAll(Client.INST.getProfile().toString(), Client.INST.getProfile().toColoredString());
            Client.INST.getConsole().history.add("§f[§2IRC§f] " + message);
        }
    }

    public void sendIRCMessage(String text) {
        chatChannel.sendMessage(text).queue();
    }

    public void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }
}