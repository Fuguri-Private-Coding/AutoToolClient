package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;

@ModuleInfo(name = "FovModifier", category = Category.VISUAL)
public class FovModifier extends Module {

    public IntegerSetting fov = new IntegerSetting("Fov", this,10,150,100);

    public CheckBox dynamicFov = new CheckBox("Dynamic Fov", this, false);



}
