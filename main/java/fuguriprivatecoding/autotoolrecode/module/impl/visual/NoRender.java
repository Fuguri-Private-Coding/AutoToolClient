package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;

@ModuleInfo(name = "NoRender", category = Category.VISUAL, description = "Убирает не нужный рендер.")
public class NoRender extends Module {

    public CheckBox fire = new CheckBox("Fire", this, true);
}
