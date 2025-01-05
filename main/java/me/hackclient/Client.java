package me.hackclient;

import me.hackclient.cfg.ConfigManager;
import me.hackclient.event.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.ObjectsCaller;
import me.hackclient.event.events.KeyEvent;
import me.hackclient.friend.FriendManager;
import me.hackclient.guis.altManager.AltManagerGuiScreen;
import me.hackclient.guis.clickGui.ClickGuiScreen;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleManager;
import me.hackclient.module.impl.combat.killaura.click.ClickManager;
import me.hackclient.scheduler.time.TimeScheduler;
import me.hackclient.shader.ShaderManager;
import org.lwjgl.opengl.Display;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public enum Client implements CallableObject {
	INSTANCE;

	final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

	TimeScheduler timeScheduler;
	FriendManager friendManager;
	ModuleManager moduleManager;
	ShaderManager shaderManager;
	ObjectsCaller objectsCaller;
	ConfigManager configManager;

	ClickGuiScreen clickGui;

	ClickManager clickManager;

	public void init() {
		callables.add(this);

		timeScheduler = new TimeScheduler();
		friendManager = new FriendManager();
		moduleManager = new ModuleManager();
		shaderManager = new ShaderManager();
		objectsCaller = new ObjectsCaller();
		configManager = new ConfigManager();

		clickGui = new ClickGuiScreen();

		clickManager = new ClickManager();
		Display.setTitle(getFullName());
	}

	public String getName() {
		return "AutoTool";
	}

	public String getVersion() {
		return "1.0";
	}

	// Это надо для того, что-бы не писать каждый раз "getName() + " " + getVersion();", а получать сразу название + версия
	public String getFullName() {
		return getName() + " " + getVersion();
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public ShaderManager getShaderManager() {
		return shaderManager;
	}

	public ObjectsCaller getObjectsCaller() {
		return objectsCaller;
	}

	public FriendManager getFriendManager() {
		return friendManager;
	}

	public TimeScheduler getTimeScheduler() {
		return timeScheduler;
	}

	public ClickGuiScreen getClickGui() {
		return clickGui;
	}

	public ScheduledExecutorService getExecutor() {
		return executorService;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof KeyEvent keyEvent) {
			for (Module module : moduleManager.modules) {
				if (module.getKey() == keyEvent.getKey()) {
					module.toggle();
				}
			}
		}
	}
}
