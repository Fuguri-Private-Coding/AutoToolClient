package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "HurtCamera", category = Category.VISUAL, description = "Позволяет изменить тряску при ударе.")
public class HurtCamera extends Module {
    public FloatSetting strength = new FloatSetting("Strength", this, 0, 1, 0, 0.1f);
}
