package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;

@ModuleInfo(name = "ClickSettings", category = Category.COMBAT, description = "Позволяет бить в тайминг.")
public class ClickSettings extends Module {

    public DoubleSlider startHurtTime = new DoubleSlider("StartHurtTime", this, 0,10,3,1);
    public DoubleSlider endHurtTime = new DoubleSlider("EndHurtTime", this, 0,10,3,1);

    public CheckBox forceClickReduce = new CheckBox("ForceClickReduce", this);
    public IntegerSetting minDiffToForce = new IntegerSetting("MinDiffToForce", this, forceClickReduce::isToggled, 0, 180, 45);

    public CheckBox noFriendDamage = new CheckBox("NoFriendDamage", this);

    public CheckBox ignoreWalls = new CheckBox("IgnoreWalls", this, false);

}
