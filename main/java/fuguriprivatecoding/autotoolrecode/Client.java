package fuguriprivatecoding.autotoolrecode;

import de.florianmichael.viamcp.ViaMCP;
import Effekseer.installer.LoadNatives;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigGuiScreen;
import fuguriprivatecoding.autotoolrecode.config.ConfigManager;
import fuguriprivatecoding.autotoolrecode.command.CommandManager;
import fuguriprivatecoding.autotoolrecode.deeplearn.DeepLearningEngine;
import fuguriprivatecoding.autotoolrecode.event.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.*;
import fuguriprivatecoding.autotoolrecode.guis.console.*;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientMainMenu;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.managers.*;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleManager;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.utils.font.*;
import fuguriprivatecoding.autotoolrecode.utils.hwid.HWIDUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.discord.*;
import fuguriprivatecoding.autotoolrecode.utils.file.*;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.sound.SoundsManager;
import fuguriprivatecoding.autotoolrecode.utils.version.ClientVersion;
import net.dv8tion.jda.api.entities.Message;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
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
	GuiClientMainMenu mainMenu;
	LoadNatives loadNatives;
	@Setter Discord discord;
	@Setter ClientIRC irc;

	FontsRepository fonts;

	boolean starting = false;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

		name = "AutoTool";
		version = new ClientVersion(4, 1,2);

		updateClient();
		connect();

		clientDirectory = new File(name);
		modelsDirectory = new File(name + "/models");
		soundsDirectory = new File(name + "/sounds");

		FileUtils.createIfNotExists(clientDirectory, modelsDirectory, soundsDirectory);

		eventManager = new EventManager();
		eventManager.register(this);

		WindowIconHelper.setWindowIcon(
				new ResourceLocation("minecraft", "hackclient/image/logo16.png"),
				new ResourceLocation("minecraft", "hackclient/image/logo32.png")
		);

		console = new ConsoleGuiScreen();

		discord = new Discord();
		combatManager = new CombatManager();
		friendManager = new FriendManager();
        soundsManager = new SoundsManager();

//		loadNatives = new LoadNatives();
//		loadNatives.init();

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

		clickGui = new ClickGuiScreen();

		configGuiScreen = new ConfigGuiScreen();

		mc.gameSettings.ofFastRender = false;
		Display.setTitle(getFullName());

		mainMenu = new GuiClientMainMenu();

		discord.init();
		discord.startRPC();

        starting = false;

		double elapsedNanos = System.nanoTime() - start;
		console.log("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

	 private void updateClient() {
		for (Message message : irc.getClientVersionChannel().getIterableHistory().stream().toList()) {
			if (!message.getContentRaw().equalsIgnoreCase(version.toString())) {
				JOptionPane.showMessageDialog(null, "Твоя версия клиента устарела: " + version.toString() + ", Пожалуйста обновите клиент до: " + message.getContentRaw());
				System.exit(-1);
			}
		}
	}

	public String getChangeLog() {
		return """
				
				""";
	}

	public void onClose() {
		configManager.saveConfig(configManager.getDefaultConfig());
		configManager.saveBinds();
		disconnect();
	}

	public String getFullName() {
		return getName() + " " + getVersion();
	}

	private long lastTime;

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent) {
			if (!IRC.usersOnline.isEmpty()) IRC.usersOnline.clear();
			join();
		}
		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
			new Thread(HWIDUtils::check).start();
			//if (discord.getId() != null) IRC.setDiscordProfile(Client.INST.getDiscord().getId());
		}
		if (event instanceof KeyEvent keyEvent) {
			for (Module module : moduleManager.getModules()) {
				if (module.getKey() == keyEvent.getKey()) {
					module.toggle();
				}
			}
		}
	}

	public void connect() {
		irc.getOnlineChannel().sendMessage(
				Client.INST.getProfile().toString() + " " + version.toString()
		).queue(sendMessage -> ClientIRC.myOnlineID = sendMessage.getIdLong());
	}

	public void disconnect() {
		if (ClientIRC.myOnlineID != -1) irc.getOnlineChannel().deleteMessageById(ClientIRC.myOnlineID).queue();
		if (ClientIRC.myID != -1) irc.getServerChannel().deleteMessageById(ClientIRC.myID).queue();
	}

	public void join() {
		if (ClientIRC.myID != -1) {
			Client.INST.getIrc().getServerChannel().deleteMessageById(ClientIRC.myID).queue(_ -> {
				ClientIRC.myID = -1;
				Client.INST.getIrc().getServerChannel().sendMessage(
						mc.getSession().getUsername() + " " + Client.INST.getProfile()
				).queue(sendMessage -> ClientIRC.myID = sendMessage.getIdLong());
			});
		} else {
			Client.INST.getIrc().getServerChannel().sendMessage(
					mc.getSession().getUsername() + " " + Client.INST.getProfile()
			).queue(sendMessage -> ClientIRC.myID = sendMessage.getIdLong());
		}
	}
}
