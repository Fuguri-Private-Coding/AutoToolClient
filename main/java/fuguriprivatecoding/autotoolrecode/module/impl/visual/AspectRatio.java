package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "AspectRatio", category = Category.VISUAL, description = "Растягивает экран.")
public class AspectRatio extends Module {
    public final FloatSetting aspect = new FloatSetting("Aspect",this, 0f, 10.0f, 1.0f, 0.01f);
    public final CheckBox aspectHand = new CheckBox("AspectHand", this);
}
