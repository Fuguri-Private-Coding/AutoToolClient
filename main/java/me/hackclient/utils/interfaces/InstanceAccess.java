package me.hackclient.utils.interfaces;

import me.hackclient.module.ModuleManager;
import me.hackclient.shader.ShaderRenderType;
import me.hackclient.shader.impl.BloomUtils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public interface InstanceAccess {

	Minecraft mc = Minecraft.getMinecraft();
	ModuleManager mm = ModuleManager.INSTANCE;

	List<Runnable> NORMAL_BlOOM_RUNNABLE = new ArrayList<>();

	static void	render2DRunnable() {
		BloomUtils.run(ShaderRenderType.OVERLAY, InstanceAccess.NORMAL_BlOOM_RUNNABLE);
	}

	static void render3DRunnable() {
		BloomUtils.run(ShaderRenderType.CAMERA, InstanceAccess.NORMAL_BlOOM_RUNNABLE);
	}

	static void clearRunnable() {
		NORMAL_BlOOM_RUNNABLE.clear();
	}

}
