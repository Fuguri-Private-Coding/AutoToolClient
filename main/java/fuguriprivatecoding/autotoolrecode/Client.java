package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.alt.AccountManager;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.*;
import fuguriprivatecoding.autotoolrecode.guis.config.*;
import fuguriprivatecoding.autotoolrecode.config.*;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.utils.sound.*;
import fuguriprivatecoding.autotoolrecode.command.*;
import fuguriprivatecoding.autotoolrecode.event.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.*;
import fuguriprivatecoding.autotoolrecode.guis.console.*;
import fuguriprivatecoding.autotoolrecode.guis.main.*;
import fuguriprivatecoding.autotoolrecode.irc.*;
import fuguriprivatecoding.autotoolrecode.managers.*;
import fuguriprivatecoding.autotoolrecode.module.*;
import fuguriprivatecoding.autotoolrecode.utils.font.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.discord.*;
import fuguriprivatecoding.autotoolrecode.utils.file.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.version.ClientVersion;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.hwid.HWIDUtils;
import fuguriprivatecoding.autotoolrecode.module.Module;
import net.dv8tion.jda.api.entities.Message;
import org.lwjgl.opengl.Display;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
public enum Client implements Imports {
	INST;

	String name = "AutoTool";
	final ClientVersion version = new ClientVersion(4, 5,1);

	@Setter
	Profile profile;

	File clientDirectory;
	File modelsDirectory;
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

	ClickGuiScreen clickGui;
	ConfigGuiScreen configGuiScreen;
	AltManagerGuiScreen altManagerGui;
	GuiClientMainMenu mainMenu;

	NameGenerator generator;

	@Setter Discord discord;
	@Setter ClientIRC irc;

	List<Message> changeLogList;

	boolean starting = true;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

		name = "AutoTool";

		Display.setTitle(getFullName());

		updateClient();
        irc.connectClient();

		clientDirectory = new File(name);
		modelsDirectory = new File(name + "/models");
		soundsDirectory = new File(name + "/sounds");

		FileUtils.createDirectoriesIfNotExists(clientDirectory, modelsDirectory, soundsDirectory);

		eventManager = new EventManager();
		eventManager.register(this);

		console = new ConsoleGuiScreen();

        Fonts.init();

		discord = new Discord();

		combatManager = new CombatManager();
		friendManager = new FriendManager();
		soundsManager = new SoundsManager();

		moduleManager = new ModuleManager();

		shaderManager = new ShaderManager();
		shaderManager.init();

		configManager = new ConfigManager();
		configManager.init();
		configManager.loadBinds();

        AccountManager.init();

		generator = new NameGenerator("names.txt");

		altManagerGui = new AltManagerGuiScreen();

		commandManager = new CommandManager();
		clickManager = new ClickManager();

		new PositionResolverComponent();

        new PlayerManager();

		ViaMCP.create();

		clickGui = new ClickGuiScreen();

		configGuiScreen = new ConfigGuiScreen();

		mc.gameSettings.ofFastRender = false;

		mainMenu = new GuiClientMainMenu();

		discord.init();
		discord.startRPC();

		configManager.loadConfig(configManager.getDefaultConfig());

		starting = false;

		double elapsedNanos = System.nanoTime() - start;
		console.log("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

	private void updateClient() {
		for (Message message : irc.getClientVersionChannel().getIterableHistory().stream().toList()) {
			if (!message.getContentRaw().equalsIgnoreCase(version.toString())) {
				JOptionPane.showMessageDialog(null, "Твоя версия клиента устарела: " + version + ", Пожалуйста обновите клиент до: " + message.getContentRaw());
				System.exit(-1);
			}
		}
	}

	public void onClose() {
		configManager.saveConfig(configManager.getDefaultConfig());
		configManager.saveBinds();
        irc.disconnectClient();
	}

	public String getFullName() {
		return getName() + " " + getVersion();
	}

	private long lastTime;

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent && moduleManager.getModule(IRC.class).isToggled()) irc.connectServer();
		if (event instanceof KeyEvent keyEvent) handleKeyEventForModules(keyEvent);
		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
			new Thread(HWIDUtils::check).start();
		}
	}

	private void handleKeyEventForModules(KeyEvent keyEvent) {
		for (Module module : moduleManager.getModules()) {
			if (module.getKey() == keyEvent.getKey()) module.toggle();
		}
	}

}
