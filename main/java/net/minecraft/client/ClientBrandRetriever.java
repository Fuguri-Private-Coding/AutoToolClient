package net.minecraft.client;

import me.hackclient.Client;
import me.hackclient.module.impl.misc.ClientSpoofer;

public class ClientBrandRetriever
{
    public static String getClientModName()
    {
        final ClientSpoofer clientSpoofer = Client.INSTANCE.getModuleManager().getModule(ClientSpoofer.class);
        if (clientSpoofer.isToggled()) {
            return clientSpoofer.brand.getMode();
        } else {
            return "vanilla";
        }
    }
}
