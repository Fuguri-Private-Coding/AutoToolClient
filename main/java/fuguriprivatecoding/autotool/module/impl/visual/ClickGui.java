package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.ColorSetting;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGui extends Module {

	private final Mode mode = new Mode("Mode", this)
			.addModes("New", "Old")
			.setMode("Old");

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public FloatSetting backgroundRadius = new FloatSetting("BackgroundRadius", this, 0.5f, 10, 2, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 240, 100);
	public FloatSetting toggleModuleVolume = new FloatSetting("ToggleModuleVolume", this, 0.1f, 1, 1, 0.1f) {};

	public final ColorSetting color = new ColorSetting("Color", this, 0f,0.5f,1f,1f);

	@Override
	public void onEnable() {
		mc.displayGuiScreen(Client.INST.getClickGui());
		toggle();
	}
}
