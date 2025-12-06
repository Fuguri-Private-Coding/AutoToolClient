package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;

@ModuleInfo(name = "FovModifier", category = Category.VISUAL, description = "Настройки фова.")
public class FovModifier extends Module {

    public IntegerSetting fov = new IntegerSetting("Fov", this,10,150,100);
    public CheckBox dynamicFov = new CheckBox("DynamicFov", this, false);
}
