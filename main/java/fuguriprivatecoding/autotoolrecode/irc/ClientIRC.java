package fuguriprivatecoding.autotoolrecode.irc;

import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.Map;
import java.util.function.Consumer;

public class ClientIRC extends ListenerAdapter implements Imports {

    @Getter @Setter
    public static MessageChannel chatChannel, loginChannel, serverChannel, keyChannel,
        onlineChannel, onlineConfigsChannel, clientVersionChannel,
        fontsChannel;

    @Getter @Setter
    public static long MESSAGE_ID = -1, ONLINE_MESSAGE_ID = -1;

    private static JDA jda;

    public static void init() {
        new ClientIRC();

        try {
            jda.awaitReady();
            initializeChannels(jda);
        } catch (InterruptedException e) {
            System.out.println("Failed connect to server.");
            System.exit(-1);
        }
    }

    private ClientIRC() {
        {
        String token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GGhvgp.juE97JuncYJRgH-Rzca0OV2a8ieMd2g6XzV1IA";
            try {
                jda = JDABuilder.createDefault(token)
                    .addEventListeners(this).build();
            } catch (Exception e) {
                System.out.println("Failed create connection.");
                System.exit(-1);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Client.INST.isStarting()) return;

        MessageChannel currentChannel = event.getChannel();

        if (currentChannel == chatChannel) {
            String message = event.getMessage().getContentRaw();
            message = message.replaceAll(Client.INST.getProfile().toString(), Client.INST.getProfile().toColoredString());
            ConsoleScreen.logWithoutPrefix("§f[§2IRC§f] " + message);
        }
    }

    private static void initializeChannels(JDA jda) {
        Map<String, Consumer<MessageChannel>> channelConfigurators = Map.of(
            "hwid-list", ClientIRC::setKeyChannel,
            "online-users", ClientIRC::setOnlineChannel,
            "online-configs", ClientIRC::setOnlineConfigsChannel,
            "client-version", ClientIRC::setClientVersionChannel,
            "login-log", ClientIRC::setLoginChannel,
            "irc-chat", ClientIRC::setChatChannel,
            "server-log", ClientIRC::setServerChannel,
            "fonts", ClientIRC::setFontsChannel
        );

        for (Guild guild : jda.getGuilds()) {
            for (MessageChannel channel : guild.getTextChannels()) {
                String channelName = channel.getName();
                Consumer<MessageChannel> configurator = channelConfigurators.get(channelName);
                if (configurator != null) {
                    configurator.accept(channel);
                }
            }
        }
    }

    public static void connectClient() {
        MessageChannel onlineChannel = getOnlineChannel();
        String messageContent = Client.INST.getProfile().toString() + " " + Client.INST.getCLIENT_VERSION();

        onlineChannel.sendMessage(messageContent).queue(sendMessage -> ClientIRC.ONLINE_MESSAGE_ID = sendMessage.getIdLong());
    }

    public static void disconnectClientServer() {
        if (ClientIRC.ONLINE_MESSAGE_ID != -1) getOnlineChannel().deleteMessageById(ClientIRC.ONLINE_MESSAGE_ID).queue();
        if (ClientIRC.MESSAGE_ID != -1) getServerChannel().deleteMessageById(ClientIRC.MESSAGE_ID).queue();
    }

    public static void disconnectServer() {
        if (ClientIRC.MESSAGE_ID != -1) getServerChannel().deleteMessageById(ClientIRC.MESSAGE_ID).queue(_ -> ClientIRC.MESSAGE_ID = -1);
    }

    public static void connectServer() {
        if (ClientIRC.MESSAGE_ID != -1) {
            updateExistingServerConnection();
        } else {
            createNewServerConnection();
        }
    }

    private static void updateExistingServerConnection() {
        MessageChannel serverChannel = ClientIRC.getServerChannel();

        serverChannel.deleteMessageById(ClientIRC.MESSAGE_ID).queue(_ -> {
            ClientIRC.MESSAGE_ID = -1;
            sendServerConnectionMessage(serverChannel);
        });
    }

    private static void createNewServerConnection() {
        MessageChannel serverChannel = ClientIRC.getServerChannel();
        sendServerConnectionMessage(serverChannel);
    }

    private static void sendServerConnectionMessage(MessageChannel channel) {
        String username = mc.getSession().getUsername();
        Profile profile = Client.INST.getProfile();
        String messageContent = username + " " + profile;

        channel.sendMessage(messageContent).queue(sendMessage -> ClientIRC.MESSAGE_ID = sendMessage.getIdLong());
    }

    public static void sendIRCMessage(String text) {
        chatChannel.sendMessage(text).queue();
    }

    public static void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }
}