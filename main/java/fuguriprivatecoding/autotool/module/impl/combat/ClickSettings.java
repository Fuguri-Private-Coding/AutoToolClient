package fuguriprivatecoding.autotool.module.impl.combat;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;

@ModuleInfo(name = "ClickSettings", category = Category.COMBAT, hide = true)
public class ClickSettings extends Module {

    public final IntegerSetting minStartHurtTime = new IntegerSetting("MinStartHurtTime", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (maxStartHurtTime.value < value) { value = maxStartHurtTime.value; }
            return super.getValue();
        }
    };
    public final IntegerSetting maxStartHurtTime = new IntegerSetting("MaxStartHurtTime", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (minStartHurtTime.value > value) { value = minStartHurtTime.value; }
            return super.getValue();
        }
    };
}
