package me.hackclient.utils.interfaces;

import me.hackclient.module.ModuleManager;
import me.hackclient.module.impl.visual.Shadows;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public interface InstanceAccess {

	Minecraft mc = Minecraft.getMinecraft();
	ModuleManager mm = ModuleManager.INSTANCE;

	List<Runnable> NORMAL_BlOOM_RUNNABLES = new ArrayList<>();

	static void clearRunnables() {
		NORMAL_BlOOM_RUNNABLES.clear();
	}

	}
