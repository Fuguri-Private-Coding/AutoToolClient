package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;

@ModuleInfo(name = "Blur", category = Category.VISUAL, description = "Размытие экрана.")
public class Blur extends Module {

    public MultiMode module = new MultiMode("Modules", this)
            .addModes("ClickGui","ConsoleGui", "ConfigGui",
                    "Scaffold", "ChestESP", "BedESP","BlockOverlay", "HotKeyGui", "ArrayList"
            );

    public IntegerSetting radius = new IntegerSetting("Radius", this, 6, 70, 6);
    public FloatSetting offset1 = new FloatSetting("Offset1", this, 0, 10, 2, 0.1f);
    public FloatSetting offset2 = new FloatSetting("Offset2", this, 0, 10, 2, 0.1f);
}
