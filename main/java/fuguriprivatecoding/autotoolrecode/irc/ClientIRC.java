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
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ClientIRC extends ListenerAdapter implements Imports {

    @Getter @Setter
    public static MessageChannel chatChannel, loginChannel, serverChannel, keyChannel,
        onlineChannel, onlineConfigsChannel, clientVersionChannel,
        fontsChannel;

    private static final AtomicLong MESSAGE_ID = new AtomicLong(-1);
    private static final AtomicLong ONLINE_MESSAGE_ID = new AtomicLong(-1);

    private static JDA jda;

    private static final Map<String, Consumer<MessageChannel>> CHANNEL_SETTERS = Map.of(
        "hwid-list", ClientIRC::setKeyChannel,
        "online-users", ClientIRC::setOnlineChannel,
        "online-configs", ClientIRC::setOnlineConfigsChannel,
        "client-version", ClientIRC::setClientVersionChannel,
        "login-log", ClientIRC::setLoginChannel,
        "irc-chat", ClientIRC::setChatChannel,
        "server-log", ClientIRC::setServerChannel,
        "fonts", ClientIRC::setFontsChannel
    );

    public static void init() {
        new ClientIRC();

        try {
            jda.awaitStatus(JDA.Status.CONNECTED);
            initializeChannels(jda);
            System.out.println("[" + Client.CLIENT_NAME + "] " + "Успешно подключено к серверу.");
        } catch (InterruptedException e) {
            System.out.println("[" + Client.CLIENT_NAME + "] " + "Не удалось подключится к серверу.");
            System.exit(-1);
        }
    }

    private ClientIRC() {
        {
        String token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GGhvgp.juE97JuncYJRgH-Rzca0OV2a8ieMd2g6XzV1IA";
            try {
                jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .setEnableShutdownHook(false)
                    .build();

                System.out.println("[" + Client.CLIENT_NAME + "] " + "Подключение к серверу...");
            } catch (Exception e) {
                System.out.println("[" + Client.CLIENT_NAME + "] " + "Не удалось создать подключение.");
                System.exit(-1);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Client.starting) return;

        MessageChannel currentChannel = event.getChannel();

        if (currentChannel == chatChannel) {
            String message = event.getMessage().getContentRaw();
            message = message.replaceAll(Client.profile.toString(), Client.profile.toColoredString());
            ConsoleScreen.logWithoutPrefix("§f[§2IRC§f] " + message);
        }
    }

    private static void initializeChannels(JDA jda) {
        jda.getGuilds().stream()
            .flatMap(guild -> guild.getTextChannels().stream())
            .forEach(channel -> {
                Consumer<MessageChannel> setter = CHANNEL_SETTERS.get(channel.getName());
                if (setter != null) {
                    setter.accept(channel);
                }
            });
    }

    public static void connectClient() {
        MessageChannel onlineChannel = getOnlineChannel();
        String messageContent = Client.profile.toString() + " " + Client.CLIENT_VERSION;

        onlineChannel.sendMessage(messageContent).queue(
            message -> ONLINE_MESSAGE_ID.set(message.getIdLong()),
            _ -> ONLINE_MESSAGE_ID.set(-1)
        );
    }

    public static void disconnectClient() {
        long onlineMessageId = ONLINE_MESSAGE_ID.get();
        if (onlineMessageId != -1) {
            getOnlineChannel().deleteMessageById(onlineMessageId).queue(
                _ -> ONLINE_MESSAGE_ID.set(-1),
                throwable -> {
                    if (throwable instanceof ErrorResponseException ex) {
                        if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                            ONLINE_MESSAGE_ID.set(-1);
                        }
                    }
                }
            );
        }
    }

    public static void disconnectServer() {
        long messageId = MESSAGE_ID.get();
        if (messageId != -1) {
            getServerChannel().deleteMessageById(messageId).queue(
                _ -> MESSAGE_ID.set(-1),
                throwable -> {
                    if (throwable instanceof ErrorResponseException ex) {
                        if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                            MESSAGE_ID.set(-1);
                        }
                    }
                }
            );
        }
    }

    public static void connectServer() {
        long messageId = MESSAGE_ID.get();
        if (messageId != -1) {
            updateExistingServerConnection();
        } else {
            createNewServerConnection();
        }
    }

    private static void updateExistingServerConnection() {
        MessageChannel serverChannel = ClientIRC.getServerChannel();
        long messageId = MESSAGE_ID.get();

        serverChannel.deleteMessageById(messageId).queue(
            _ -> {
                MESSAGE_ID.set(-1);
                sendServerConnectionMessage(serverChannel);
            },
            throwable -> {
                if (throwable instanceof ErrorResponseException ex) {
                    if (ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        MESSAGE_ID.set(-1);
                        sendServerConnectionMessage(serverChannel);
                    }
                } else {
                    MESSAGE_ID.set(-1);
                    sendServerConnectionMessage(serverChannel);
                }
            }
        );
    }

    private static void createNewServerConnection() {
        MessageChannel serverChannel = ClientIRC.getServerChannel();
        sendServerConnectionMessage(serverChannel);
    }

    private static void sendServerConnectionMessage(MessageChannel channel) {
        String username = mc.getSession().getUsername();
        Profile profile = Client.profile;
        String messageContent = username + " " + profile;

        channel.sendMessage(messageContent).queue(
            message -> MESSAGE_ID.set(message.getIdLong()),
            _ -> MESSAGE_ID.set(-1)
        );
    }

    public static void sendIRCMessage(String text) {
        chatChannel.sendMessage(text).queue();
    }

    public static void sendMessage(MessageChannel channel, String text) {
        channel.sendMessage(text).queue();
    }

    public static void shutdown() {
        try {
            disconnectClient();
            Thread.sleep(1000);
            disconnectServer();
            jda.awaitShutdown(Duration.ofSeconds(1));
        } catch (Exception ignored) {}
    }
}