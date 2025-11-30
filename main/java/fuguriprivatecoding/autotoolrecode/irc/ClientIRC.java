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
    public MessageChannel chatChannel, loginChannel, serverChannel, keyChannel,
        onlineChannel, changeLogChannel, onlineConfigsChannel, clientVersionChannel,
        clientCapesChannel, fontsChannel;

    @Getter @Setter
    public static long MESSAGE_ID = -1, ONLINE_MESSAGE_ID = -1;

    String token;

    public JDA jda;

    public void init() {
        token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GGhvgp.juE97JuncYJRgH-Rzca0OV2a8ieMd2g6XzV1IA";

        try {
            jda = JDABuilder.createDefault(token).addEventListeners(this).build();
        } catch (Exception e) {
            System.out.println("Failed create connection.");
            System.exit(-1);
        }

        try {
            jda.awaitReady();
            initializeChannels(jda);
        } catch (InterruptedException e) {
            System.out.println("Failed connect to server.");
            System.exit(-1);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Client.INST.isStarting()) return;

        MessageChannel currentChannel = event.getChannel();

        if (currentChannel == chatChannel) {
            String message = event.getMessage().getContentRaw();
            message = message.replaceAll(Client.INST.getProfile().toString(), Client.INST.getProfile().toColoredString());
            ConsoleScreen.history.add("§f[§2IRC§f] " + message);
        }
    }

    public void initializeChannels(JDA jda) {
        Map<String, Consumer<MessageChannel>> channelConfigurators = Map.of(
            "hwid-list", this::setKeyChannel,
            "online-users", this::setOnlineChannel,
            "change-log", this::setChangeLogChannel,
            "online-configs", this::setOnlineConfigsChannel,
            "client-version", this::setClientVersionChannel,
            "client-capes", this::setClientCapesChannel,
            "login-log", this::setLoginChannel,
            "irc-chat", this::setChatChannel,
            "server-log", this::setServerChannel,
            "fonts", this::setFontsChannel
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

    public void connectClient() {
        MessageChannel onlineChannel = getOnlineChannel();
        String messageContent = Client.INST.getProfile().toString() + " " + Client.INST.getCLIENT_VERSION();

        onlineChannel.sendMessage(messageContent).queue(sendMessage -> ClientIRC.ONLINE_MESSAGE_ID = sendMessage.getIdLong());
    }

    public void disconnectClientServer() {
        if (ClientIRC.ONLINE_MESSAGE_ID != -1) getOnlineChannel().deleteMessageById(ClientIRC.ONLINE_MESSAGE_ID).queue();
        if (ClientIRC.MESSAGE_ID != -1) getServerChannel().deleteMessageById(ClientIRC.MESSAGE_ID).queue();
    }

    public void disconnectServer() {
        if (ClientIRC.MESSAGE_ID != -1) getServerChannel().deleteMessageById(ClientIRC.MESSAGE_ID).queue(_ -> ClientIRC.MESSAGE_ID = -1);
    }

    public void connectServer() {
        if (ClientIRC.MESSAGE_ID != -1) {
            updateExistingServerConnection();
        } else {
            createNewServerConnection();
        }
    }

    private void updateExistingServerConnection() {
        MessageChannel serverChannel = Client.INST.getIrc().getServerChannel();

        serverChannel.deleteMessageById(ClientIRC.MESSAGE_ID).queue(_ -> {
            ClientIRC.MESSAGE_ID = -1;
            sendServerConnectionMessage(serverChannel);
        });
    }

    private void createNewServerConnection() {
        MessageChannel serverChannel = Client.INST.getIrc().getServerChannel();
        sendServerConnectionMessage(serverChannel);
    }

    private void sendServerConnectionMessage(MessageChannel channel) {
        String username = mc.getSession().getUsername();
        Profile profile = Client.INST.getProfile();
        String messageContent = username + " " + profile;

        channel.sendMessage(messageContent).queue(sendMessage -> ClientIRC.MESSAGE_ID = sendMessage.getIdLong());
    }

    public void sendIRCMessage(String text) {
        chatChannel.sendMessage(text).queue();
    }

    public void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }
}