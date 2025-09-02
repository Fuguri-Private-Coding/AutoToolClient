package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;

@ModuleInfo(name = "ClickSettings", category = Category.COMBAT, description = "Позволяет бить в тайминг.")
public class ClickSettings extends Module {

    public DoubleSlider startHurtTime = new DoubleSlider("StartHurtTime", this, 0,10,3,1);
    public DoubleSlider endHurtTime = new DoubleSlider("EndHurtTime", this, 0,10,3,1);
}
