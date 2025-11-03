package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "HitBox", category = Category.COMBAT)
public class Hitbox extends Module {
    public final FloatSetting expand = new FloatSetting("Expand", this, 0.1f, 1, 0.15f, 0.01f);

    public static double getExpand() {
        Hitbox hitbox = Modules.getModule(Hitbox.class);

        if (hitbox == null || !hitbox.isToggled()) {
            return 0;
        }

        return hitbox.expand.getValue();
    }
}
