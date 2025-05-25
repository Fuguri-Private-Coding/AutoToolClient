package fuguriprivatecoding.autotoolrecode.utils.interfaces;

import fuguriprivatecoding.autotoolrecode.module.ModuleManager;
import net.minecraft.client.Minecraft;

public interface Imports {
	Minecraft mc = Minecraft.getMinecraft();
	ModuleManager mm = ModuleManager.INSTANCE;
}
