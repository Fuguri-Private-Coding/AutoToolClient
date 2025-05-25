package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.discord.Discord;

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
