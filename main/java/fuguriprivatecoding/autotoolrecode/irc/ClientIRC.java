package fuguriprivatecoding.autotoolrecode.irc;

import fuguriprivatecoding.autotoolrecode.profile.DiscordProfile;
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
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import java.awt.*;

@Getter
@Setter
public class ClientIRC extends ListenerAdapter {
    public MessageChannel chatChannel;
    public MessageChannel loginChannel;
    public MessageChannel serverChannel;
    public MessageChannel hwidChannel;
    public MessageChannel onlineChannel;
    public MessageChannel changeLogChannel;
    public MessageChannel onlineConfigsChannel;
    public MessageChannel clientVersionChannel;
    public MessageChannel clientCapesChannel;
    String token;
    public static long myID = -1;
    public static long myOnlineID = -1;
    static JDA jda;
    public static DiscordProfile profile = new DiscordProfile();

    public void init() {
        {
            {
                {
                    {
                        {
                            {
                                {
                                    token = "MTM3MjE2NTc2MTk3MTUyMzYxNQ.GGhvgp.juE97JuncYJRgH-Rzca0OV2a8ieMd2g6XzV1IA";
                                }
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
                        if (channel.getName().equalsIgnoreCase("client-version")) setClientVersionChannel(channel);
                        if (channel.getName().equalsIgnoreCase("client-capes")) setClientCapesChannel(channel);
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
            message = message.replaceAll(Client.INST.getProfile().toString(), Client.INST.getProfile().toColoredString());
            Client.INST.getConsole().history.add("§f[§2IRC§f] " + message);
        }
    }

    public static void setDiscordProfile(String userId) {
        if (jda == null || userId == null) return;

        jda.getGuilds().forEach(guild -> guild.retrieveMemberById(userId).queue(member -> {
            if (member != null) {
                member.getUser().retrieveProfile().queue(user -> {
                    try {
                        profile.setId(member.getId());
                        profile.setAvatarUrl(member.getEffectiveAvatarUrl());
                        profile.setBannerUrl(user.getBannerUrl());
                        profile.setUserName(member.getEffectiveName());
                        profile.setTag(Client.INST.getDiscord().getName());
                        profile.setProfileColor(user.getAccentColor());
                        profile.setServerRoleColor(member.getColor());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }, error -> System.out.println("Could not retrieve profile: " + error));
            }
        }, _ -> System.out.println("Member not found in guild " + guild.getName())));
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