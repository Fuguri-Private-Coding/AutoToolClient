package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "CustomCape", category = Category.VISUAL)
public class CustomCape extends Module {
    public Mode capeMode = new Mode("Mode", this)
            .addModes("Russian", "AutoTool", "LouFCat", "DollCat", "AutoToolCat", "AugustusRose", "AugustusMango", "AugustusMagma", "AugustusTitanium", "AugustusMagic", "AugustusClassic", "AugustusAmethyst", "AugustusCandy", "Augustus", "ESound")
            .setMode("Russian");

    public ResourceLocation getCape() {
        return new ResourceLocation("minecraft", "hackclient/capes/" + capeMode.getMode().toLowerCase() + ".png");
    }
}
