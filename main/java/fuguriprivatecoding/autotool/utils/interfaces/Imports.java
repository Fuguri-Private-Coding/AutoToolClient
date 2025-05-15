package fuguriprivatecoding.autotool.utils.interfaces;

import fuguriprivatecoding.autotool.module.ModuleManager;
import net.minecraft.client.Minecraft;

public interface Imports {
	Minecraft mc = Minecraft.getMinecraft();
	ModuleManager mm = ModuleManager.INSTANCE;
}
