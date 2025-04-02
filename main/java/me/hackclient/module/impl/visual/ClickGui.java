package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGui extends Module {

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public FloatSetting backgroundRadius = new FloatSetting("BackgroundRadius", this, 0.5f, 10, 2, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 240, 100);
	public FloatSetting toggleModuleVolume = new FloatSetting("ToggleModuleVolume", this, 0.1f, 1, 1, 0.1f) {};

	public final ColorSetting color = new ColorSetting("Color", this, 0f,1f,1f,1f);

	@Override
	public void onEnable() {
		super.onEnable();
		mc.displayGuiScreen(Client.INSTANCE.getClickGui());
	}
}
