package me.hackclient.utils.interfaces;

import me.hackclient.module.ModuleManager;
import net.minecraft.client.Minecraft;

public interface Imports {
	Minecraft mc = Minecraft.getMinecraft();
	ModuleManager mm = ModuleManager.INSTANCE;
}
