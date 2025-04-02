package me.hackclient.module.impl.client;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.discord.Discord;


@ModuleInfo(
        name = "DiscordRPC",
        category = Category.CLIENT,
        toggled = true
)
public class DiscordRPC extends Module {

    Discord discord;

    @Override
    public void onEnable() {
        super.onEnable();
        discord.init();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        discord.stop();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) if (discord == null) discord = Client.INSTANCE.getDiscord();
    }
}
