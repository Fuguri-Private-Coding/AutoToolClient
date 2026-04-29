package fuguriprivatecoding.autotoolrecode;

import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import fuguriprivatecoding.autotoolrecode.utils.generate.NameGenerator;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;

import fuguriprivatecoding.autotoolrecode.utils.client.Discord;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.config.Configs;
import fuguriprivatecoding.autotoolrecode.bind.KeyBinds;
import fuguriprivatecoding.autotoolrecode.alt.Accounts;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.*;
import fuguriprivatecoding.autotoolrecode.utils.client.sound.*;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.*;
import fuguriprivatecoding.autotoolrecode.gui.console.*;
import fuguriprivatecoding.autotoolrecode.gui.config.*;
import fuguriprivatecoding.autotoolrecode.gui.main.*;
import fuguriprivatecoding.autotoolrecode.command.*;
import fuguriprivatecoding.autotoolrecode.handle.*;
import fuguriprivatecoding.autotoolrecode.module.*;
import fuguriprivatecoding.autotoolrecode.event.*;

import de.florianmichael.viamcp.ViaMCP;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import lombok.*;
import smtc.SmtcNative;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;

import java.io.*;

@UtilityClass
public class Client implements Imports {

	public final String CLIENT_NAME = "AutoTool";
    public final ClientVersion CLIENT_VERSION = new ClientVersion(6, 7, 0);

    private final String RESOURCES_ID = "minecraft";
    private final String RESOURCES_CLIENT_ID = "autotool/";

    public final File CLIENT_DIR = new File(CLIENT_NAME);
    private final File NATIVES_DIR = new File("natives");
    public final File SKIN_DIRECTORY = new File(CLIENT_DIR + "/skins");
    public final File CAPE_DIRECTORY = new File(CLIENT_DIR + "/capes");

	@Setter public Profile profile;

    public final MediaController MEDIA_CONTROLLER = new MediaController();

	public boolean starting = true;

	public void init() throws IOException {
		long start = System.nanoTime();
		starting = true;

        new Debl();

        File msdf_gen_file = new File(CLIENT_DIR, "msdf-gen.zip");
        FileUtils.unpackIfNeeded(msdf_gen_file, "assets/minecraft/autotool/msdf-gen/msdf-gen.zip");

        File smtc_bridge_native_file = new File(NATIVES_DIR, "smtc_bridge.dll");
        FileUtils.unpackFile(smtc_bridge_native_file, "assets/minecraft/autotool/native/smtc_bridge.dll");

        File museoSans_font = new File(Fonts.FONT_DIRECTORY, "MuseoSans.ttf");
        FileUtils.unpackFile(museoSans_font, "assets/minecraft/autotool/fonts/MuseoSans.ttf");

        File sfPro_font = new File(Fonts.FONT_DIRECTORY, "SFPro.ttf");
        FileUtils.unpackFile(sfPro_font, "assets/minecraft/autotool/fonts/SFPro.ttf");

        File sfProRegular_font = new File(Fonts.FONT_DIRECTORY, "SFProRegular.otf");
        FileUtils.unpackFile(sfProRegular_font, "assets/minecraft/autotool/fonts/SFProRegular.otf");

        File sfProRounded_font = new File(Fonts.FONT_DIRECTORY, "SFProRounded.ttf");
        FileUtils.unpackFile(sfProRounded_font, "assets/minecraft/autotool/fonts/SFProRounded.ttf");

        profile = Profile.builder().username("Ёбаный Бомж").role(Role.GOVNOED).build();

        SmtcNative.init();
        MEDIA_CONTROLLER.start();

        createDirectories();

        Display.setTitle(getFullName());

        Runtime.getRuntime().addShutdownHook(new Thread(Client::onClose));

		ConsoleScreen.init();

        Accounts.init();
        KeyBinds.init();
        Shaders.init();
        Sounds.init();
        Fonts.init();
        fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts.init();

        Modules.init();

		Configs.init();
        KeyBinds.loadBinds();

        NameGenerator.init("names.txt");

        Commands.init();
        new PositionResolverComponent();
		new Clicks();
        new Player();

		ViaMCP.create();

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
		KeyBinds.saveBinds();
        MEDIA_CONTROLLER.close();
    }

    private void createDirectories() {
        if (CLIENT_DIR.mkdirs()) ClientUtils.chatLog("Успешно создал директорию клиента.");
        if (SKIN_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию скинов.");
        if (CAPE_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию плащей.");
    }

    public String getFullName() {
        return CLIENT_NAME + " " + CLIENT_VERSION;
    }

    public ResourceLocation of(String path) {
        return new ResourceLocation(RESOURCES_ID, RESOURCES_CLIENT_ID + path);
    }
}
