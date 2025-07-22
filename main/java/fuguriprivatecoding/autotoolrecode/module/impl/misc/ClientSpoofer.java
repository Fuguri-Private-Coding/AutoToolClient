package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;

@ModuleInfo(name = "ClientSpoofer", category = Category.MISC, description = "Спуфает название клиента которое видит античит.")
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