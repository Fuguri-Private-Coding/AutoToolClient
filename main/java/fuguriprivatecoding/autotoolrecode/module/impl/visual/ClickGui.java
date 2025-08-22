package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT, description = "ХАЛЯЛЬ #НАСТРОЙКА КЛИК ГУИ ЙОУ.")
public class ClickGui extends Module {

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 255, 100);

	public final ColorSetting color = new ColorSetting("Color", this);

	@Override
	public void onEnable() {
		mc.displayGuiScreen(Client.INST.getClickGui());
		toggle();
	}
}
