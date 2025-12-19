package fuguriprivatecoding.autotoolrecode.gui.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import fuguriprivatecoding.autotoolrecode.gui.buttons.Button;
import fuguriprivatecoding.autotoolrecode.gui.buttons.ImgButton;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainScreen extends GuiScreen {

    public static MainScreen INST;

    public static void init() {
        INST = new MainScreen();
    }

    private MainScreen() {
        this.mc = Imports.mc;
    }

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

    EasingAnimation openAnim = new EasingAnimation();

    EasingAnimation anim = new EasingAnimation();

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new Button(0,"SinglePlayer", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10, 150, 20));
        buttonList.add(new Button(1,"MultiPlayer", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25, 150, 20));
        buttonList.add(new Button(2,"MinecraftSettings", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25, 150, 20));
        buttonList.add(new Button(3,"AltManager", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25 + 25, 150, 20));
        buttonList.add(new ImgButton(4, exitLogo, sc.getScaledWidth() - 20,5, 15, 15));
        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        final ClientFont font = Fonts.fonts.get("SFPro");

        final String hello = "Привет §a" + Client.INST.getProfile().toColoredString() + ".";

        final String welcome = "Добро пожаловать в §9AutoTool§f!";

        openAnim.update(3f, Easing.IN_OUT_QUAD);

        BackgroundUtils.run();

        AlphaUtils.startWrite();

        boolean isHovered = GuiUtils.isHovered(mouseX, mouseY, sc.getScaledWidth() / 2f - 80, sc.getScaledHeight() / 2f - 25, 160, 25f);

        anim.update(1.5f, Easing.OUT_BACK);
        anim.setEnd(isHovered);

        float heightAnim = 15 * anim.getValue();

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, sc.getScaledWidth() / 2f - 80, sc.getScaledHeight() / 2f - 25 - heightAnim, 160, 25f + 4 + heightAnim);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 80, sc.getScaledHeight() / 2f - 25 - heightAnim, 160, 25f + heightAnim, 2, 12.5f, 12.5f, 2, Colors.BLACK.withAlpha(0.7f));
        String discord = "нах ты открыл, ладно нажимай.";
        boolean isHoveredText = GuiUtils.isHovered(mouseX, mouseY, sc.getScaledWidth() / 2f - font.getStringWidth(discord) / 2f, sc.getScaledHeight() / 2f + 2 - heightAnim, font.getStringWidth(discord), 9);

        Color hoveredColor = isHoveredText && anim.getValue() == 1 ? Colors.RED.withAlphaClamp(anim.getValue()) : Colors.WHITE.withAlphaClamp(anim.getValue());
        font.drawCenteredString(discord, sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f + 5 - heightAnim, hoveredColor);

        ScissorUtils.disableScissor();

        font.drawCenteredString(hello, sc.getScaledWidth() / 2f + 1.5f, sc.getScaledHeight() / 2f - 5 - 15 - heightAnim, Color.WHITE);
        font.drawCenteredString(welcome, sc.getScaledWidth() / 2f + 1.5f, sc.getScaledHeight() / 2f - 5 - 5f - heightAnim, Color.WHITE);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 80, sc.getScaledHeight() / 2f + 5, 160, 105, 15, 2, 2, 15, Colors.BLACK.withAlpha(0.7f));

        super.drawScreen(mouseX, mouseY, partialTicks);

        AlphaUtils.endWrite();
        AlphaUtils.draw(openAnim.getValue());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        openAnim.setValue(0f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);
        ClientFont font = Fonts.fonts.get("SFPro");
        String discord = "нах ты открыл, ладно нажимай.";

        float heightAnim = 15 * anim.getValue();

        boolean isHoveredText = GuiUtils.isHovered(mouseX, mouseY, sc.getScaledWidth() / 2f - font.getStringWidth(discord) / 2f, sc.getScaledHeight() / 2f + 2 - heightAnim, font.getStringWidth(discord), 9);

        if (isHoveredText && mouseButton == 0 && anim.getValue() == 1) {
            String url = "https://discord.gg/yuu5f5J8mv";
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException _) {}
        }

        super.mouseClicked(mouseX,mouseY,mouseButton);
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
