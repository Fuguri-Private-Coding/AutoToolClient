package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "Reach", category = Category.COMBAT, description = "Увеличивает дальность вашего удара.")
public class Reach extends Module {
    public final FloatSetting addRange = new FloatSetting("Add range", this, -3, 3, 1.5f, 0.01f);

    public static double getAddRange() {
        Reach reach = Modules.getModule(Reach.class);

        if (reach == null || !reach.isToggled()) {
            return 0;
        }

        return reach.addRange.getValue();
    }
}
