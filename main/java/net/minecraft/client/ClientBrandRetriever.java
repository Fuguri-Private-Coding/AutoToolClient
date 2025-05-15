package net.minecraft.client;

import me.hackclient.Client;
import me.hackclient.module.impl.misc.ClientSpoofer;

public class ClientBrandRetriever
{
    public static String getClientModName()
    {
        final ClientSpoofer clientSpoofer = Client.INST.getModuleManager().getModule(ClientSpoofer.class);
        if (clientSpoofer.isToggled()) {
            return clientSpoofer.getBrand();
        } else {
            return "vanilla";
        }
    }
}
