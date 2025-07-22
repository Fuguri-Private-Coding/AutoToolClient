package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;

@ModuleInfo(name = "ClientSettings", category = Category.CLIENT, description = "Модуль где вы можете подробно настраивать клиент.")
public class ClientSettings extends Module {

    public FloatSetting toggleModuleVolume = new FloatSetting("ToggleModuleVolume", this, 0.1f, 1, 1, 0.1f) {};
    public FloatSetting backgroundRadius = new FloatSetting("BackgroundGuiRadius", this, 0.5f, 7, 7, 0.1f) {};

}
