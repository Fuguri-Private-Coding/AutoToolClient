package me.hackclient.module.impl.misc;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;

@ModuleInfo(name = "ClientSpoofer", category = Category.MISC, toggled = true)
public class ClientSpoofer extends Module {

    public ModeSetting brand = new ModeSetting("Brand", this, "LunarClient",
            new String[]{"LunarClient", "LabyMod", "FML", "Optifine", "Forge"});

    public String getBrand() {
        switch (brand.getMode()) {
            case "LunarClient" -> {
                return "Lunar-Client";
            }

            case "LabyMod" -> {
                return "labymod";
            }

            case "Optifine" -> {
                return "optifine";
            }

            case "FML" -> {
                return "fml,forge";
            }

            case "Forge" -> {
                return "forge";
            }
        }
        return "";
    }

    @Override
    public String getSuffix() {
        return brand.getMode();
    }
}