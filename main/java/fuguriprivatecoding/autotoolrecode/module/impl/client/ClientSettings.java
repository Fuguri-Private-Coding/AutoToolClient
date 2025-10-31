package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "ClientSettings", category = Category.CLIENT, description = "Модуль где вы можете подробно настраивать клиент.", toggled = true)
public class ClientSettings extends Module {

    public FloatSetting toggleModuleVolume = new FloatSetting("ToggleModuleVolume", this, 0.1f, 1, 1, 0.1f) {};
    public FloatSetting backgroundRadius = new FloatSetting("BackgroundGuiRadius", this, 0.5f, 7, 7, 0.1f) {};
    public IntegerSetting scroll = new IntegerSetting("ScrollStep", this, -50, 50, 10);
    public FloatSetting scale = new FloatSetting("Scale", this, 0.5f, 2, 1, 0.01f) {};

    public static int getScroll() {
        ClientSettings clientSettings = Client.INST.getModules().getModule(ClientSettings.class);
        int currentScroll = Mouse.getDWheel();

        return currentScroll / 120 * clientSettings.scroll.getValue();
    }
}
