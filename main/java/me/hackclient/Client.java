package me.hackclient;

import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.config.ConfigManager;
import me.hackclient.command.CommandManager;
import me.hackclient.deeplearn.DeepLearningEngine;
import me.hackclient.event.EventManager;
import me.hackclient.event.EventTarget;
import me.hackclient.guis.console.ConsoleGuiScreen;
import me.hackclient.managers.CombatManager;
import me.hackclient.event.Event;
import me.hackclient.event.events.KeyEvent;
import me.hackclient.managers.FriendManager;
import me.hackclient.guis.clickgui.ClickGuiScreen;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleManager;
import me.hackclient.managers.ClickManager;
import me.hackclient.shader.ShaderManager;
import me.hackclient.utils.discord.Discord;
import me.hackclient.utils.file.FileUtils;
import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.packet.PositionResolverComponent;
import me.hackclient.utils.profile.Profile;
import me.hackclient.utils.sound.SoundsManager;
import me.hackclient.utils.version.ClientVersion;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;

@Getter
public enum Client implements Imports {
	INSTANCE;

	String name;
	ClientVersion version;

	@Setter
	Profile profile;

	File clientDirectory;
	File modelsDirectory;
	File capesDirectory;

	File soundsDirectory;

	ConsoleGuiScreen console;
	SoundsManager soundsManager;
	EventManager eventManager;
	CombatManager combatManager;
	FriendManager friendManager;
    ModuleManager moduleManager;
    ShaderManager shaderManager;
    ConfigManager configManager;
    CommandManager commandManager;
    ClickManager clickManager;
	DeepLearningEngine deepLearningEngine;
	ClickGuiScreen clickGui;
	Discord discord;

	public void init() throws IOException {
		long start = System.nanoTime();

		name = "AutoTool";
		version = new ClientVersion(2, 1, 1);

		clientDirectory = new File(name);
		modelsDirectory = new File(name + "/models");
		soundsDirectory = new File(name + "/sounds");
		capesDirectory = new File(name + "/capes");

		FileUtils.createIfNotExists(clientDirectory, modelsDirectory, soundsDirectory, capesDirectory);

		eventManager = new EventManager();
		eventManager.register(this);
		console = new ConsoleGuiScreen();

		combatManager = new CombatManager();
		friendManager = new FriendManager();
        soundsManager = new SoundsManager();

		try {
			discord = new Discord();
			discord.init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		moduleManager = new ModuleManager();
		shaderManager = new ShaderManager();
		configManager = new ConfigManager();
		configManager.init();
		configManager.loadConfig(configManager.getDefaultConfig());
		configManager.loadBinds();
		commandManager = new CommandManager();
		clickManager = new ClickManager();
		deepLearningEngine = new DeepLearningEngine();
		deepLearningEngine.init();

		new PositionResolverComponent();

		try {
			ViaMCP.create();
			ViaMCP.INSTANCE.initAsyncSlider();
		} catch (Exception e) {
			e.printStackTrace();
		}

		clickGui = new ClickGuiScreen();

		mc.gameSettings.ofFastRender = false;
		Display.setTitle(getFullName());

		double elapsedNanos = System.nanoTime() - start;
		console.log("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

	public String getChangeLog() {
		return """
				HWID System
				""";
	}

	public void onClose() {
		configManager.saveConfig(configManager.getDefaultConfig());
		configManager.saveBinds();
	}

	public String getFullName() {
		return getName() + " " + getVersion();
	}

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof KeyEvent keyEvent) {
			for (Module module : moduleManager.getModules()) {
				if (module.getKey() == keyEvent.getKey()) {
					module.toggle();
				}
			}
		}
	}
}
