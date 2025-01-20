package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(
        name = "CustomCape",
        category = Category.VISUAL,
        toggled = true
)
public class CustomCape extends Module {

    public ModeSetting capeMode = new ModeSetting(
            "Mode",
            this,
            "Russian",
            new String[] {
                    "Russian",
                    "AutoTool",
            }
    );

    public ResourceLocation getCape() {
        return new ResourceLocation("minecraft", "hackclient/capes/" + capeMode.getMode().toLowerCase() + ".png");
    }
}
