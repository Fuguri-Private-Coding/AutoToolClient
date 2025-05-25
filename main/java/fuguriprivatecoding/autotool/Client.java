package fuguriprivatecoding.autotool;

import de.florianmichael.viamcp.ViaMCP;
import fuguriprivatecoding.autotool.irc.packet.ClientSocket;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.config.ConfigManager;
import fuguriprivatecoding.autotool.command.CommandManager;
import fuguriprivatecoding.autotool.deeplearn.DeepLearningEngine;
import fuguriprivatecoding.autotool.event.EventManager;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotool.guis.clickgui.NewClickGuiScreen;
import fuguriprivatecoding.autotool.guis.console.ConsoleGuiScreen;
import fuguriprivatecoding.autotool.managers.CombatManager;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.events.KeyEvent;
import fuguriprivatecoding.autotool.managers.FriendManager;
import fuguriprivatecoding.autotool.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleManager;
import fuguriprivatecoding.autotool.managers.ClickManager;
import fuguriprivatecoding.autotool.utils.font.FontsRepository;
import fuguriprivatecoding.autotool.utils.render.shader.ShaderManager;
import fuguriprivatecoding.autotool.utils.discord.Discord;
import fuguriprivatecoding.autotool.utils.file.FileUtils;
import fuguriprivatecoding.autotool.utils.hwid.HWIDUtils;
import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import fuguriprivatecoding.autotool.utils.packet.PositionResolverComponent;
import fuguriprivatecoding.autotool.profile.Profile;
import fuguriprivatecoding.autotool.utils.sound.SoundsManager;
import fuguriprivatecoding.autotool.utils.version.ClientVersion;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;

@Getter
public enum Client implements Imports {
	INST;

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
	NewClickGuiScreen newClickGuiScreen;
	Discord discord;
	@Setter ClientSocket clientSocket;

	FontsRepository fonts;

	boolean starting = false;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

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

		discord = new Discord();
		discord.init();

		moduleManager = new ModuleManager();

		shaderManager = new ShaderManager();
		shaderManager.init();

		configManager = new ConfigManager();
		configManager.init();
		configManager.loadConfig(configManager.getDefaultConfig());
		configManager.loadBinds();

		commandManager = new CommandManager();
		clickManager = new ClickManager();

		deepLearningEngine = new DeepLearningEngine();
		deepLearningEngine.init();

		new PositionResolverComponent();

		ViaMCP.create();
		ViaMCP.INSTANCE.initAsyncSlider();

//		fonts = new FontsRepository();
//		Fonts.init();

		clickGui = new ClickGuiScreen();

		mc.gameSettings.ofFastRender = false;
		Display.setTitle(getFullName());

		starting = false;

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

	private long lastTime;

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
			//new Thread(HWIDUtils::check).start();
		}
		if (event instanceof KeyEvent keyEvent) {
			for (Module module : moduleManager.getModules()) {
				if (module.getKey() == keyEvent.getKey()) {
					module.toggle();
				}
			}
		}
	}
}
