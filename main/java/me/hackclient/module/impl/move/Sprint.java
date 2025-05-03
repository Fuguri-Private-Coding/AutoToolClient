package me.hackclient.module.impl.move;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.client.entity.EntityPlayerSP;

@ModuleInfo(name = "Sprint", category = Category.MOVE)
public class Sprint extends Module {
	@Override
	public void toggle() {
		super.toggle();
		EntityPlayerSP.forceSprint = isToggled();
	}
}
