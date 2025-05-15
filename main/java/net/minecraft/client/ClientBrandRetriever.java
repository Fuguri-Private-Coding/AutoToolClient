package net.minecraft.client;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.module.impl.misc.ClientSpoofer;

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
