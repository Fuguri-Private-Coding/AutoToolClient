package me.hackclient.module.impl.move;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.client.entity.EntityPlayerSP;

@ModuleInfo(name = "Sprint", category = Category.MOVE, toggled = true)
public class SprintModule extends Module {
	public SprintModule() {
		super();
	}

	public void onEnable() {
		EntityPlayerSP.forceSprint = true;
	}

	public void onDisable() {
		EntityPlayerSP.forceSprint = false;
	}
}
