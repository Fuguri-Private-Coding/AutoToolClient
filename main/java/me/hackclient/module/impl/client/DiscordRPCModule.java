package me.hackclient.module.impl.client;

import me.hackclient.Client;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.discord.Discord;

@ModuleInfo(
        name = "DiscordRPC",
        category = Category.CLIENT
)
public class DiscordRPCModule extends Module {
    Discord discord;

    public DiscordRPCModule() {
        discord = Client.INSTANCE.getDiscord();
    }

    @Override
    public void onEnable() {
        discord.init();
    }

    @Override
    public void onDisable() {
        discord.stop();
    }
}
