package me.hackclient;

import me.hackclient.event.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.ObjectsCaller;
import me.hackclient.event.events.KeyEvent;
import me.hackclient.friend.FriendManager;
import me.hackclient.guis.clickGui.ClickGuiScreen;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleManager;
import me.hackclient.scheduler.time.TimeScheduler;
import me.hackclient.shader.ShaderManager;
import org.lwjgl.opengl.Display;

public enum Client implements CallableObject {
	INSTANCE;

	ClickGuiScreen clickGui;

	TimeScheduler timeScheduler;
	FriendManager friendManager;
	ModuleManager moduleManager;
	ShaderManager shaderManager;
	ObjectsCaller objectsCaller;

	public void init() {
		callables.add(this);
		timeScheduler = new TimeScheduler();
		friendManager = new FriendManager();
		objectsCaller = new ObjectsCaller();
		moduleManager = new ModuleManager();
		shaderManager = new ShaderManager();

		clickGui = new ClickGuiScreen();
		Display.setTitle(getFullName());
	}

	public String getName() {
		return "AutoToolClient";
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

	public ClickGuiScreen getClickGui() {
		return clickGui;
	}

	public TimeScheduler getTimeScheduler() {
		return timeScheduler;
	}

	public void onEvent(Event event) {
		if (event instanceof KeyEvent keyEvent) {
			for (Module module : moduleManager.modules) {
				if (module.getKey() == keyEvent.getKey()) {
					module.toggle();
				}
			}
		}
	}

	@Override
	public boolean handleEvents() {
		return true;
	}
}
