package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;

@ModuleInfo(name = "Blur", category = Category.VISUAL)
public class Blur extends Module {

    public MultiMode module = new MultiMode("Modules", this)
            .addModes("ClickGui","ConsoleGui", "ConfigGui",
                    "Scaffold", "ChestESP", "BedESP","ArrayList", "FPSCounter",
                    "BPSCounter", "BreakIndicator", "BlockOverlay"
            );

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 35, 6);
    public FloatSetting horizontal1Compress = new FloatSetting("Horizontal1Compress", this, 0, 10, 2, 0.1f);
    public FloatSetting vertical1Compress = new FloatSetting("Vertical1Compress", this, 0, 10, 0, 0.1f);
    public FloatSetting vertical2Compress = new FloatSetting("Vertical2Compress", this, 0, 10, 2, 0.1f);

}
