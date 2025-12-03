package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.ServerJoinEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWID;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
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

import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import lombok.*;

import java.awt.*;
import java.io.*;

@Getter
public enum Client implements Imports, EventListener {
	INST;

	public final String CLIENT_NAME = "AutoTool";
    public final ClientVersion CLIENT_VERSION = new ClientVersion(4, 5,3);

    private final String RESOURCES_ID = "minecraft";
    private final String RESOURCES_CLIENT_ID = "autotool/";

    public final File CLIENT_DIR = new File(CLIENT_NAME);

	@Setter Profile profile;

	boolean starting = true;

    private long lastTime;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

        Display.setTitle(getFullName());

        VersionCheck.validateClientVersion(ClientIRC.getClientVersionChannel());

        Runtime.getRuntime().addShutdownHook(new Thread(this::onClose));

        ClientIRC.connectClient();

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
		ConsoleScreen.log("Клиент инициализировался за " + (float) (elapsedNanos / 1000000000D) + " секунд.");
	}

	public void onClose() {
		Configs.saveConfig(Configs.getDefaultConfig());
		KeyBinds.saveBinds();
        ClientIRC.disconnectClientServer();
	}

    @Override
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent && Modules.getModule(IRC.class).isToggled()) ClientIRC.connectServer();
		if (event instanceof KeyEvent keyEvent) {
            for (Module module : Modules.getModules()) {
                if (module.getKey() == keyEvent.getKey()) module.toggle();
            }
        }

		if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 10000) {
			lastTime = System.currentTimeMillis();
            new Thread(HWID::check).start();
		}

        if (event instanceof RenderScreenEvent && HWID.noConnection) {
            long time = System.currentTimeMillis() - HWID.lastTimeConnection;

            ClientFontRenderer fontRenderer = Fonts.fonts.get("SFPro");
            ScaledResolution sc = new ScaledResolution(mc);

            int sec = Integer.parseInt(String.valueOf(time / 1000L));

            String text = "§f[§9AutoTool§f] Нет интернет подключения, клиент закроется при §930 §fсекундах: §9" + sec + "§f s.";

            fontRenderer.drawCenteredString(text, sc.getScaledWidth() / 2f, 5, Color.WHITE);
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
