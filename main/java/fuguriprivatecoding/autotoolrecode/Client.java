package fuguriprivatecoding.autotoolrecode;

import de.florianmichael.viamcp.ViaMCP;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigGuiScreen;
import fuguriprivatecoding.autotoolrecode.irc.packet.ClientSocket;
import fuguriprivatecoding.autotoolrecode.config.ConfigManager;
import fuguriprivatecoding.autotoolrecode.command.CommandManager;
import fuguriprivatecoding.autotoolrecode.deeplearn.DeepLearningEngine;
import fuguriprivatecoding.autotoolrecode.event.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.*;
import fuguriprivatecoding.autotoolrecode.guis.console.*;
import fuguriprivatecoding.autotoolrecode.managers.*;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleManager;
import fuguriprivatecoding.autotoolrecode.utils.font.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.discord.*;
import fuguriprivatecoding.autotoolrecode.utils.file.*;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.sound.SoundsManager;
import fuguriprivatecoding.autotoolrecode.utils.version.ClientVersion;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import lombok.Getter;
import lombok.Setter;
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
	ConfigGuiScreen configGuiScreen;
	AltManagerGuiScreen altManagerGui;
	NewClickGuiScreen newClickGuiScreen;
	Discord discord;
	@Setter ClientSocket clientSocket;

	FontsRepository fonts;

	boolean starting = false;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

		name = "AutoTool";
		version = new ClientVersion(2, 4,0);

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

		altManagerGui = new AltManagerGuiScreen();

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

		configGuiScreen = new ConfigGuiScreen();

		mc.gameSettings.ofFastRender = false;
		Display.setTitle(getFullName());

        starting = false;

		WindowIconHelper.setWindowIcon(
				new ResourceLocation("minecraft", "hackclient/image/logo16.png"),
				new ResourceLocation("minecraft", "hackclient/image/logo32.png")
		);

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
