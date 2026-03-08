package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.alts.AltScreen;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.ServerJoinEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.DynamicIsland;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWID;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.utils.client.Discord;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.config.Configs;
import fuguriprivatecoding.autotoolrecode.bind.KeyBinds;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.alts.Accounts;

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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import lombok.*;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public enum Client implements Imports, EventListener {
	INST;

	public final String CLIENT_NAME = "AutoTool";
    public final ClientVersion CLIENT_VERSION = new ClientVersion(4, 6, 3);

    private final String RESOURCES_ID = "minecraft";
    private final String RESOURCES_CLIENT_ID = "autotool/";

    public final File CLIENT_DIR = new File(CLIENT_NAME);
    public final File SKIN_DIRECTORY = new File(CLIENT_DIR + "/skins");
    public final File CAPE_DIRECTORY = new File(CLIENT_DIR + "/capes");

	@Setter Profile profile;

	boolean starting = true;

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

        scheduler.scheduleAtFixedRate(
            HWID::check,
            0,
            10,
            TimeUnit.SECONDS
        );

        if (this.profile == null) System.exit(-1);

        createDirectories();

        Display.setTitle(getFullName());

        VersionCheck.validateClientVersion(ClientIRC.getClientVersionChannel());

        Runtime.getRuntime().addShutdownHook(new Thread(this::onClose));

        ClientIRC.connectClient();

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
		ConsoleScreen.log("Клиент инициализировался за " + String.format("%.2f", (float) (elapsedNanos / 1000000000D)) + " секунд.");
	}

	public void onClose() {
		Configs.saveConfig(Configs.getDefaultConfig());
//        Accounts.save();
		KeyBinds.saveBinds();
        ClientIRC.disconnectClientServer();
        scheduler.shutdown();
    }

    private void createDirectories() {
        if (CLIENT_DIR.mkdirs()) System.out.println("Successful created Client Directory.");
        if (SKIN_DIRECTORY.mkdirs()) System.out.println("Successful created Skin Directory.");
        if (CAPE_DIRECTORY.mkdirs()) System.out.println("Successful created Cape Directory.");
    }

    @Override
	public void onEvent(Event event) {
		if (event instanceof ServerJoinEvent && Modules.getModule(IRC.class).isToggled()) ClientIRC.connectServer();
		if (event instanceof KeyEvent keyEvent) {
            for (Module module : Modules.getModules()) {
                if (module.getKey() == keyEvent.getKey()) module.toggle();
            }
        }

        if (event instanceof RenderScreenEvent && !Modules.getModule(DynamicIsland.class).isToggled()) {
            ClientFont fontRenderer = Fonts.fonts.get("SFPro");
            ScaledResolution sc = new ScaledResolution(mc);

            EasingAnimation anim = HWID.noConnectionAnim;
            anim.update(1f, Easing.OUT_BACK);

            long time = System.currentTimeMillis() - HWID.lastTimeConnection;
            int sec = Integer.parseInt(String.valueOf(time / 1000L));

            int remainingSec = 30 - sec;

            String text = ClientUtils.prefixLog + "Нет интернет подключения, клиент закроется через §9" + remainingSec + "§f s.";

            float x = sc.getScaledWidth() / 2f - fontRenderer.getStringWidth(text) / 2f - 5;
            float y = 5;

            float width = fontRenderer.getStringWidth(text);
            float height = 15;

            ScaleUtils.startScaling(x, y, width, height, anim.getValue());

            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - width / 2f - 5, 5f, width + 5, 15f, 7.5f, Colors.BLACK.withAlphaClamp(0.7f * anim.getValue()));
            fontRenderer.drawCenteredString(text, sc.getScaledWidth() / 2f, 10, Colors.WHITE.withAlphaClamp(anim.getValue()));
            ScaleUtils.stopScaling();
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
