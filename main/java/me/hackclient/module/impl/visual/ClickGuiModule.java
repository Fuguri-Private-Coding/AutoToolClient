package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSettings;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT)
public class ClickGuiModule extends Module {

	public FloatSettings animationSpeed = new FloatSettings("AnimationSpeed", this, 1, 20, 10, 0.5f);

	@Override
	public void onEnable() {
		super.onEnable();
		mc.displayGuiScreen(Client.INSTANCE.getClickGui());
	}
}
