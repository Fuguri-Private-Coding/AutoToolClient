package me.hackclient;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.cfg.ConfigManager;
import me.hackclient.managers.CombatManager;
import me.hackclient.event.callable.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.ObjectsCaller;
import me.hackclient.event.events.KeyEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.managers.FriendManager;
import me.hackclient.guis.clickGui.ClickGuiScreen;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleManager;
import me.hackclient.managers.ClickManager;
import me.hackclient.scheduler.time.TimeScheduler;
import me.hackclient.shader.ShaderManager;
import me.hackclient.utils.sound.SoundsManager;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public enum Client implements CallableObject {
	INSTANCE;

	final File clientDirectory = new File("AutoTool");
	final File configsDirectory = new File(clientDirectory, "configs");
	final File bindsDirectory = new File(clientDirectory, "binds");
	final File soundsDirectory = new File(clientDirectory, "sounds");
	@Setter File defaultConfig = new File(configsDirectory, "default.json");
	@Setter File defaultBinds = new File(bindsDirectory, "binds.json");

	ScheduledExecutorService executorService;

	CombatManager combatManager;
	TimeScheduler timeScheduler;
	FriendManager friendManager;
	ModuleManager moduleManager;
	ShaderManager shaderManager;
	ObjectsCaller objectsCaller;
	ConfigManager configManager;
	SoundsManager soundsManager;

	ClickGuiScreen clickGui;

	ClickManager clickManager;

	long lastMS;

	public void init() throws IOException {
		long start = System.nanoTime();

		if (!clientDirectory.exists()) {
			clientDirectory.mkdirs();
			System.out.println("Create client directory");
		}

		if (!configsDirectory.exists()) {
			configsDirectory.mkdirs();
			System.out.println("Create config directory");
		}

		if (!bindsDirectory.exists()) {
			bindsDirectory.mkdirs();
			System.out.println("Create bind directory");
		}

		if (!soundsDirectory.exists()) {
			soundsDirectory.mkdirs();
			System.out.println("Create sound directory");
		}

		callables.add(this);

		executorService = Executors.newScheduledThreadPool(4);

		combatManager = new CombatManager();
		timeScheduler = new TimeScheduler();
		friendManager = new FriendManager();
		moduleManager = new ModuleManager();
		shaderManager = new ShaderManager();
		objectsCaller = new ObjectsCaller();
		configManager = new ConfigManager();

		try {
			configManager.load(defaultConfig);
		} catch (IOException e) {
			System.out.println("Error while loading cfg");
		}

		try {
			configManager.loadBinds(defaultBinds);
		} catch (IOException e) {
			System.out.println("Error while loading binds");
		}

		clickGui = new ClickGuiScreen();

		clickManager = new ClickManager();

		soundsManager = new SoundsManager();

		Display.setTitle(getFullName());

		double elapsedNanos = System.nanoTime() - start;
		System.out.println("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

	public String getChangeLog() {
		return """
				Added changelog
				Recoded Ping
				Recoded BackTrack
				Added ClickSettings
				Added KBLager (test)
				Recoded TimerRangeV2
				""";
	}

	public void onClose() {
		saveAll();
	}

	public void saveAll() {
		try {
			configManager.save(defaultConfig);
		} catch (IOException e) {
			System.out.println("Error while saving cfg");
		}

		try {
			configManager.saveBinds(defaultBinds);
		} catch (IOException e) {
			System.out.println("Error while saving binds");
		}
	}

	public String getName() {
		return "AutoTool";
	}

	public String getVersion() {
		return "1.1";
	}

	public String getFullName() {
		return getName() + " " + getVersion();
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
		if (event instanceof RunGameLoopEvent) {
			if (System.currentTimeMillis() - lastMS >= 15000) {
				saveAll();
				lastMS = System.currentTimeMillis();
			}
		}
	}
}
