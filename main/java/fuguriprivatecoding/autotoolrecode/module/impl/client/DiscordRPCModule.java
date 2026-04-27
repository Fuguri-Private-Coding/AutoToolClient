package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.client.Discord;

@ModuleInfo(
        name = "DiscordRPC",
        category = Category.CLIENT,
        description = "Мульти донксинг халяль клиент ДИСКОРД РПС"
)
public class DiscordRPCModule extends Module {

    @Override
    public void onEnable() {
        if (!Client.starting) Discord.start();
    }

    @Override
    public void onDisable() {
        Discord.stop();
    }
}
