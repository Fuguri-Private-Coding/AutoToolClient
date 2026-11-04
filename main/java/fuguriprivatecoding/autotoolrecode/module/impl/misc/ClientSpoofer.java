package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "ClientSpoofer", category = Category.MISC, description = "Спуфает название клиента которое видит античит.")
public class ClientSpoofer extends Module {

    public Mode brand = new Mode("Brand", this)
            .addModes("LunarClient", "LabyMod", "Optifine", "Badlion")
            .setMode("Badlion");

    public String getBrand() {
        switch (brand.getMode()) {
            case "Badlion" -> {
                return "badlion";
            }
            case "LunarClient" -> {
                return "lunaclient:v2.23.12-2644";
            }

            case "LabyMod" -> {
                return "labymod";
            }

            case "Optifine" -> {
                return "optifine";
            }
        }
        return "";
    }
}