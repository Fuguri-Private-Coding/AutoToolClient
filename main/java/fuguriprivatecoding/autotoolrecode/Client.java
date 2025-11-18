package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.alt.Accounts;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.*;
import fuguriprivatecoding.autotoolrecode.gui.config.*;
import fuguriprivatecoding.autotoolrecode.config.*;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.utils.client.Discord;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.sound.*;
import fuguriprivatecoding.autotoolrecode.command.*;
import fuguriprivatecoding.autotoolrecode.event.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.*;
import fuguriprivatecoding.autotoolrecode.gui.console.*;
import fuguriprivatecoding.autotoolrecode.gui.main.*;
import fuguriprivatecoding.autotoolrecode.irc.*;
import fuguriprivatecoding.autotoolrecode.handle.*;
import fuguriprivatecoding.autotoolrecode.module.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.file.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
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

@Getter
public enum Client implements Imports, EventListener {
	INST;

	String name = "AutoTool";
	final ClientVersion version = new ClientVersion(4, 5,2);

	@Setter
	Profile profile;

	File clientDir;
	File soundsDir;

	@Setter ClientIRC irc;

	boolean starting = true;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

        setDisplayInfo();
		updateClient();

        irc.connectClient();

		clientDir = new File(name);
		soundsDir = new File(name + "/sounds");

		FileUtils.createDirectoriesIfNotExists(clientDir, soundsDir);

		Events.register(this);

		ConsoleScreen.init();

        Fonts.init();
        Sounds.init();
        Shaders.init();
        Accounts.init();

        Modules.init();

		Configs.init();
		Configs.loadBinds();

        NameGenerator.init("names.txt");

        Commands.init();
		new Clicks();
        new Player();
        new PositionResolverComponent();

		ViaMCP.create();

        AltScreen.init();
        ClickScreen.init();
        ClickGuiScreenNew.init();
        ConsoleScreen.init();
		ConfigScreen.init();
        MainScreen.init();

		mc.gameSettings.ofFastRender = false;

        new Discord();

		Configs.loadConfig(Configs.getDefaultConfig());

		starting = false;

		double elapsedNanos = System.nanoTime() - start;
		ConsoleScreen.log("Started client in " + (float) (elapsedNanos / 1000000000D) + " seconds");
	}

    private void setDisplayInfo() {
        name = "AutoTool";
        Display.setTitle(getFullName());
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
		Configs.saveConfig(Configs.getDefaultConfig());
		Configs.saveBinds();
        irc.disconnectClient();
	}

	public String getFullName() {
		return getName() + " " + getVersion();
	}

	private long lastTime;

    @Override
    public boolean listen() {
        return true;
    }

    @Override
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent && Modules.getModule(IRC.class).isToggled()) irc.connectServer();
		if (event instanceof KeyEvent keyEvent) handleKeyEventForModules(keyEvent);
		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
			new Thread(HWIDUtils::check).start();
		}
	}

	private void handleKeyEventForModules(KeyEvent keyEvent) {
		for (Module module : Modules.getModules()) {
			if (module.getKey() == keyEvent.getKey()) module.toggle();
		}
	}

}
