package fuguriprivatecoding.autotoolrecode.gui.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.gui.altmanager.AltScreen;
import fuguriprivatecoding.autotoolrecode.gui.buttons.Button;
import fuguriprivatecoding.autotoolrecode.gui.buttons.ImgButton;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
import java.io.IOException;

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

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new Button(0,"Single Player", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10, 150, 20));
        buttonList.add(new Button(1,"Multi Player", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25, 150, 20));
        buttonList.add(new Button(2,"Minecraft Settings", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25, 150, 20));
        buttonList.add(new Button(3,"Alt Manager", sc.getScaledWidth() / 2f - 75,sc.getScaledHeight() / 2f + 10 + 25 + 25 + 25, 150, 20));
        buttonList.add(new ImgButton(4, exitLogo, sc.getScaledWidth() - 20,5, 15, 15));
        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        final ClientFontRenderer font = Fonts.fonts.get("SFPro");
        final String user = "Пользователь: [§9" + Client.INST.getProfile().getUsername() + "§f]";
        final String role = "Роль: [" + Client.INST.getProfile().getRole().getColorPrefix() + Client.INST.getProfile().getRole() + "§f]";

        final String welcome = "Привет.  Добро пожаловать в AutoTool!";

        openAnim.update(3, Easing.IN_OUT_QUAD);

        BackgroundUtils.run();

        AlphaUtils.startWrite();

        font.drawString(user,5, 5, Color.WHITE);
        font.drawString(role,5, 5 + 10, Color.WHITE);
        font.drawCenteredString(welcome, sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f - 5, Color.WHITE);
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
