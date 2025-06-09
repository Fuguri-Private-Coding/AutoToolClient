package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "CustomCape", category = Category.VISUAL)
public class CustomCape extends Module {
    public Mode capeMode = new Mode("Mode", this)
            .addModes("Russian", "AutoTool", "LouFCat", "DollCat", "AutoToolCat", "Felix", "AugustusRose", "AugustusMango", "AugustusMagma", "AugustusTitanium", "AugustusMagic", "AugustusClassic", "AugustusAmethyst", "AugustusCandy", "Augustus", "ESound")
            .setMode("Russian");

    public ResourceLocation getCape() {
        return new ResourceLocation("minecraft", "hackclient/capes/" + capeMode.getMode().toLowerCase() + ".png");
    }
}
