package fuguriprivatecoding.autotool.module.impl.client;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.utils.discord.Discord;

@ModuleInfo(
        name = "DiscordRPC",
        category = Category.CLIENT
)
public class DiscordRPCModule extends Module {
    Discord discord;

    public DiscordRPCModule() {
        discord = Client.INST.getDiscord();
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
