package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "ItemPhysics", category = Category.VISUAL, description = "Анимирует выкинутые вещи.")
public class ItemPhysics extends Module {
    public FloatSetting rotateMultiplier = new FloatSetting("RotateMultiplier", this, 0, 2, 1, 0.01f);

}
