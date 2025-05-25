package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGui extends Module {

	private final Mode mode = new Mode("Mode", this)
			.addModes("New", "Old")
			.setMode("Old");

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public FloatSetting backgroundRadius = new FloatSetting("BackgroundRadius", this, 0.5f, 10, 2, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 255, 100);
	public FloatSetting toggleModuleVolume = new FloatSetting("ToggleModuleVolume", this, 0.1f, 1, 1, 0.1f) {};

	public final CheckBox fadeColor = new CheckBox("FadeColor", this);
	public final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
	public final ColorSetting color2 = new ColorSetting("Color2", this, fadeColor::isToggled, 1f,1f,1f,1f);
	public final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeColor::isToggled,0.1f, 20, 1, 0.1f);

	@Override
	public void onEnable() {
		mc.displayGuiScreen(Client.INST.getClickGui());
		toggle();
	}
}
