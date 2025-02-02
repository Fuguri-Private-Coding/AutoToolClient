package me.hackclient;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.cfg.ConfigManager;
import me.hackclient.combatmanager.CombatManager;
import me.hackclient.event.callable.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.ObjectsCaller;
import me.hackclient.event.events.KeyEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.friend.FriendManager;
import me.hackclient.guis.clickGui.ClickGuiScreen;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleManager;
import me.hackclient.module.impl.combat.killaura.click.ClickManager;
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

	final File clientDirectory = new File("AutoTool/configs");
	final File soundsDirectory = new File("AutoTool/sounds");
	@Setter File defaultConfig = new File(clientDirectory, "default.json");
	@Setter File bindsDirectory = new File(clientDirectory, "binds.json");

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

		callables.add(this);

		executorService = Executors.newScheduledThreadPool(4);

		combatManager = new CombatManager();
		timeScheduler = new TimeScheduler();
		friendManager = new FriendManager();
		moduleManager = new ModuleManager();
		shaderManager = new ShaderManager();
		objectsCaller = new ObjectsCaller();
		configManager = new ConfigManager();
		soundsManager = new SoundsManager();

		try {
			configManager.load(defaultConfig);
		} catch (IOException e) {
			System.out.println("Error while loading cfg");
			throw new RuntimeException(e);
		}

		try {
			configManager.loadBinds(bindsDirectory);
		} catch (IOException e) {
			System.out.println("Error while loading binds");
			throw new RuntimeException(e);
		}

		clickGui = new ClickGuiScreen();

		clickManager = new ClickManager();
		Display.setTitle(getFullName());

		long elapsedNanos = System.nanoTime() - start;
		System.out.println("Started client in " + (float) (elapsedNanos / 1000000000L) + " seconds");
	}

	public String getChangeLog() {
		return """
				Added changelog
				Recoded Ping
				Recoded BackTrack
				Added ClickSettings
				Added KBLagger (test)
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
			throw new RuntimeException(e);
		}

		try {
			configManager.saveBinds(bindsDirectory);
		} catch (IOException e) {
			System.out.println("Error while saving binds");
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return "AutoTool";
	}

	public String getVersion() {
		return "1.0";
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
