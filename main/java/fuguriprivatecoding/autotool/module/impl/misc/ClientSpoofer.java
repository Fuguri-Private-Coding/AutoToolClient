package fuguriprivatecoding.autotool.module.impl.misc;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.Mode;

@ModuleInfo(name = "ClientSpoofer", category = Category.MISC)
public class ClientSpoofer extends Module {

    public Mode brand = new Mode("Brand", this)
            .addModes("LunarClient", "LabyMod", "FML", "Optifine", "Forge")
            .setMode("LunarClient");

    public String getBrand() {
        switch (brand.getMode()) {
            case "LunarClient" -> {
                return "LunarClient";
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