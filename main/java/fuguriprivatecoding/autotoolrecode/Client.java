package fuguriprivatecoding.autotoolrecode;

import de.florianmichael.viamcp.ViaMCP;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigGuiScreen;
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
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRCModule;
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
	@Setter Discord discord;
	@Setter IRC irc;

	FontsRepository fonts;

	boolean starting = false;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

		name = "AutoTool";
		version = new ClientVersion(4, 0,0);

		connect();

		clientDirectory = new File(name);
		modelsDirectory = new File(name + "/models");
		soundsDirectory = new File(name + "/sounds");
		capesDirectory = new File(name + "/capes");

		FileUtils.createIfNotExists(clientDirectory, modelsDirectory, soundsDirectory, capesDirectory);

		eventManager = new EventManager();
		eventManager.register(this);

		WindowIconHelper.setWindowIcon(
				new ResourceLocation("minecraft", "hackclient/image/logo16.png"),
				new ResourceLocation("minecraft", "hackclient/image/logo32.png")
		);

		console = new ConsoleGuiScreen();

		discord = new Discord();
		discord.init();

		combatManager = new CombatManager();
		friendManager = new FriendManager();
        soundsManager = new SoundsManager();

		discord.startRPC();

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

        starting = false;

		double elapsedNanos = System.nanoTime() - start;
		console.log("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

	public String getChangeLog() {
		return """
				лень думать над канга логам так что иди нахуй тупой даун!
				
				[main] INFO net.dv8tion.jda.api.JDA - Login Successful!
				[JDA MainWS-WriteThread] INFO net.dv8tion.jda.internal.requests.WebSocketClient - Connected to WebSocket
				[JDA MainWS-ReadThread] INFO net.dv8tion.jda.api.JDA - Finished Loading!
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
			if (!IRCModule.usersOnline.isEmpty()) IRCModule.usersOnline.clear();
			join();
		}
		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
			new Thread(HWIDUtils::check).start();
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
		).queue(sendMessage -> IRC.myOnlineID = sendMessage.getIdLong());
	}

	public void disconnect() {
		if (IRC.myOnlineID != -1) irc.getOnlineChannel().deleteMessageById(IRC.myOnlineID).queue();
		if (IRC.myID != -1) irc.getServerChannel().deleteMessageById(IRC.myID).queue();
	}

	public void join() {
		if (IRC.myID != -1) {
			Client.INST.getIrc().getServerChannel().deleteMessageById(IRC.myID).queue(_ -> {
				IRC.myID = -1;
				Client.INST.getIrc().getServerChannel().sendMessage(
						mc.getSession().getUsername() + " " + Client.INST.getProfile()
				).queue(sendMessage -> IRC.myID = sendMessage.getIdLong());
			});
		} else {
			Client.INST.getIrc().getServerChannel().sendMessage(
					mc.getSession().getUsername() + " " + Client.INST.getProfile()
			).queue(sendMessage -> IRC.myID = sendMessage.getIdLong());
		}
	}
}
