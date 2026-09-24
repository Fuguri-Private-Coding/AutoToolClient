package net.minecraft.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.ClientSpoofer;

public class ClientBrandRetriever
{
    public static String getClientModName()
    {
        final ClientSpoofer clientSpoofer = Modules.getInstance().getModule(ClientSpoofer.class);
        if (clientSpoofer.isToggled()) {
            return clientSpoofer.getBrand();
        } else {
            return "vanilla";
        }
    }
}
