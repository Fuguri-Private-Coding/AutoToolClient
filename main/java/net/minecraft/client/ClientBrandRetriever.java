package net.minecraft.client;

import me.hackclient.Client;
import me.hackclient.module.impl.misc.ClientSpooferModule;

public class ClientBrandRetriever
{
    public static String getClientModName()
    {
        final ClientSpooferModule clientSpoofer = Client.INSTANCE.getModuleManager().getModule(ClientSpooferModule.class);
        if (clientSpoofer.isToggled()) {
            return clientSpoofer.brand.getMode();
        } else {
            return "vanilla";
        }
    }
}
