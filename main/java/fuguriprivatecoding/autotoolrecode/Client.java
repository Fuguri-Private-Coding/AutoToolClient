package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.event.events.world.ServerJoinEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWIDUtils;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.utils.client.Discord;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.config.Configs;
import fuguriprivatecoding.autotoolrecode.bind.KeyBinds;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.alt.Accounts;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.client.sound.*;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.gui.console.*;
import fuguriprivatecoding.autotoolrecode.gui.config.*;
import fuguriprivatecoding.autotoolrecode.gui.main.*;
import fuguriprivatecoding.autotoolrecode.command.*;
import fuguriprivatecoding.autotoolrecode.handle.*;
import fuguriprivatecoding.autotoolrecode.module.*;
import fuguriprivatecoding.autotoolrecode.event.*;
import fuguriprivatecoding.autotoolrecode.irc.*;

import net.dv8tion.jda.api.entities.Message;
import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import javax.swing.*;
import lombok.*;
import java.io.*;

@Getter
public enum Client implements Imports, EventListener {
	INST;

	public final String CLIENT_NAME = "AutoTool";
    public final ClientVersion CLIENT_VERSION = new ClientVersion(4, 5,2);

    private final String RESOURCES_ID = "minecraft";
    private final String RESOURCES_CLIENT_ID = "autotool/";

    public final File CLIENT_DIR = new File(CLIENT_NAME);

	@Setter ClientIRC irc;
	@Setter Profile profile;

	boolean starting = true;

    private long lastTime;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

        Display.setTitle(getFullName());
		updateClient();

        irc.connectClient();

        if (CLIENT_DIR.mkdirs()) System.out.println("Successful created Сlient Directory.");

		Events.register(this);

		ConsoleScreen.init();

        Accounts.init();
        KeyBinds.init();
        Shaders.init();
        Sounds.init();
        Fonts.init();

        Modules.init();

		Configs.init();
        KeyBinds.loadBinds();

        NameGenerator.init("names.txt");

        Commands.init();
        new PositionResolverComponent();
		new Clicks();
        new Player();

		ViaMCP.create();

        ClickGuiScreenNew.init();
        ConsoleScreen.init();
		ConfigScreen.init();
        ClickScreen.init();
        MainScreen.init();
        AltScreen.init();

		mc.gameSettings.ofFastRender = false;

		Configs.loadConfig(Configs.getDefaultConfig());

        Discord.init();

        starting = false;

		double elapsedNanos = System.nanoTime() - start;
		ConsoleScreen.log("Client initialized in " + (float) (elapsedNanos / 1000000000D) + " seconds.");
	}

	private void updateClient() {
		for (Message message : irc.getClientVersionChannel().getIterableHistory().stream().toList()) {
			if (!message.getContentRaw().equalsIgnoreCase(CLIENT_VERSION.toString())) {
				JOptionPane.showMessageDialog(null, "Ваша версия клиента устарела: " + CLIENT_VERSION + ", Пожалуйста обновите клиент до: " + message.getContentRaw());
				System.exit(-1);
			}
		}
	}

	public void onClose() {
		Configs.saveConfig(Configs.getDefaultConfig());
		KeyBinds.saveBinds();
        irc.disconnectClient();
	}

    @Override
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent && Modules.getModule(IRC.class).isToggled()) irc.connectServer();
		if (event instanceof KeyEvent keyEvent) {
            for (Module module : Modules.getModules()) {
                if (module.getKey() == keyEvent.getKey()) module.toggle();
            }
        }

		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
            new Thread(HWIDUtils::check).start();
		}
	}

    public String getFullName() {
        return CLIENT_NAME + " " + CLIENT_VERSION;
    }

    public ResourceLocation of(String path) {
        return new ResourceLocation(RESOURCES_ID, RESOURCES_CLIENT_ID + path);
    }

    @Override
    public boolean listen() {
        return true;
    }
}
