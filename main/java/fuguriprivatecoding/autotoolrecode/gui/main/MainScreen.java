package fuguriprivatecoding.autotoolrecode.gui.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import fuguriprivatecoding.autotoolrecode.gui.buttons.Button;
import fuguriprivatecoding.autotoolrecode.gui.buttons.ImgButton;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class MainScreen extends GuiScreen {

    public static MainScreen INST;

    public static void init() {
        INST = new MainScreen();
    }

    private MainScreen() {
        this.mc = Imports.mc;
    }

    ResourceLocation exitLogo = Client.of("image/exit.png");

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new Button(0, I18n.format("menu.singleplayer"), sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10, 150, 20));
        buttonList.add(new Button(1, I18n.format("menu.multiplayer"), sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25, 150, 20));
        buttonList.add(new Button(2, I18n.format("menu.options"), sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25, 150, 20));
        buttonList.add(new Button(3,"AltManager", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25 + 25, 150, 20));
        buttonList.add(new ImgButton(4, exitLogo, sc.getScaledWidth() - 20,5, 15, 15));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        BackgroundUtils.run();

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 80, sc.getScaledHeight() / 2f + 5, 160, 105, 15, Colors.BLACK.withAlpha(0.3f));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        final int id = button.id;

        switch (id) {
            case 0 -> mc.displayGuiScreen(new GuiSelectWorld(this));
            case 1 -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case 2 -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case 3 -> mc.displayGuiScreen(AltScreen.INST);
            case 4 -> mc.shutdownMinecraftApplet();
        }
    }
}
