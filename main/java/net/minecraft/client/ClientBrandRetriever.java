package net.minecraft.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.ClientSpoofer;

public class ClientBrandRetriever
{
    public static String getClientModName()
    {
        final ClientSpoofer clientSpoofer = Client.INST.getModules().getModule(ClientSpoofer.class);
        if (clientSpoofer.isToggled()) {
            return clientSpoofer.getBrand();
        } else {
            return "vanilla";
        }
    }
}
