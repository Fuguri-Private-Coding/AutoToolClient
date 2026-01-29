package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.Scroll;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.io.IOException;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class ClickGuiScreenNew extends GuiScreen {

    public static ClickGuiScreenNew INST;

    EasingAnimation openAnim = new EasingAnimation();

    EasingAnimation openSettingsAnim = new EasingAnimation();

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

    Category selectedCategory = Category.COMBAT;

    Module selectedModule;

    float x, y, width, height;
    float moduleScrollTotalHeight;

    boolean closing, moving;

    Scroll moduleScroll = new Scroll(30);

    public static void init() {
        INST = new ClickGuiScreenNew();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = ScaleUtils.getScaledResolution();

        x = 50;
        y = 50;
        width = sc.getScaledWidth() - 100;
        height = sc.getScaledHeight() - 100;

        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Colors rectColor = Colors.BLACK.withAlphaClamp(openAnim.getValue());
        Colors elementColor = new Colors(clickGui.color.getFadedColor()).withAlphaClamp(openAnim.getValue());
        Colors elementTextColor = Colors.WHITE.withAlphaClamp(openAnim.getValue());
        Colors elementGrayColor = new Colors(15,15,15,255).withAlphaClamp(openAnim.getValue());
        Colors elementGlowColor = new Colors(clickGui.colorShadow.getFadedColor()).withAlphaClamp(openAnim.getValue());

        ClientFont font = Fonts.fonts.get("SFPro");

        boolean hoverModules = GuiUtils.isHovered(mouseX, mouseY, x + 100, y, width - 100, height);
        moduleScroll.handleScrollInput(hoverModules);
        moduleScroll.update(moduleScrollTotalHeight + 5, height);

        ScaleUtils.startScaling(x, y, width, height, openAnim.getValue());

        updateGuiAnimations();

        if (closing && openAnim.getValue() <= 0.2) {
            closing = false;
            mc.displayGuiScreen(null);
        }

        RoundedUtils.drawRect(x + 100, y, width - 100, height, 0, 0, 10, 10, rectColor);
        RoundedUtils.drawRect(x, y, 100, height, 10, 10, 0, 0, rectColor.withAlpha(0.5f));

        BloomUtils.addToDraw(() -> {
            RenderUtils.drawMixedRoundedRect(x, y, width, height, 10f, clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
        });

        BlurUtils.addToDraw(() -> {
            RoundedUtils.drawRect(x, y, 100, height, 10, 10, 0, 0, rectColor);
        });

        GL11.glPushMatrix();
        GL11.glScaled(1.5,1.5,0);
        font.drawString(Client.INST.CLIENT_NAME, x - 2, y - 7.5f, elementColor);
        GL11.glPopMatrix();

        ClientFont catFonts = Fonts.fonts.get("SFProRegular");

        float categoryOffset = 0;
        for (Category category : Category.values()) {
            EasingAnimation toggleAnim = category.getToggleAnim();
            toggleAnim.update(4, Easing.OUT_CUBIC);
            toggleAnim.setEnd(selectedCategory == category);

            Colors categoryColor = new Colors(ColorUtils.interpolateColor(rectColor, elementColor, toggleAnim.getValue()));
            Colors categoryTextColor = new Colors(ColorUtils.interpolateColor(elementTextColor, rectColor, toggleAnim.getValue()));

            RoundedUtils.drawRect(x + 10, y + 30 + categoryOffset, 80, 12.5f, 2.5f,categoryColor);

            ColorUtils.glColor(categoryTextColor);
            RenderUtils.drawImage(category.logo, x + 10 + 1.5f, y + 30 + 1.5f + categoryOffset, 10f,10f,true);

            catFonts.drawString(category.getName(), x + 10 + 15, y + 30 + 4 + categoryOffset, categoryTextColor);
            categoryOffset += 17.5f;
        }

        ClientFont settingsFont = Fonts.fonts.get("SFProRegular");
        if (selectedModule != null) {
            float settingsOffset = 0;
            RoundedUtils.drawRect(x + 100, y, width - 100, 15, 0, 0, 10, 0, elementGrayColor);

            boolean hoverExit = GuiUtils.isHovered(mouseX, mouseY, x + width - 20, y, 15, 15);

            Color exitSettingsColor = hoverExit ? Colors.RED.withAlphaClamp(openAnim.getValue()) : Colors.WHITE.withAlphaClamp(openAnim.getValue());

            ColorUtils.glColor(exitSettingsColor);
            RenderUtils.drawImage(exitLogo, x + width - 20, y, 15, 15, true);

            font.drawString(selectedModule.getName(), x + 100 + 5, y + 6, elementTextColor);

            for (Setting setting : selectedModule.getSettings()) {
                EasingAnimation visibleAnim = setting.getVisibleAnim();
                visibleAnim.update(4f, Easing.OUT_CUBIC);
                visibleAnim.setEnd(setting.isVisible());

                if (visibleAnim.getValue() <= 0) continue;

                float settingX = x + 100 + 5;
                float settingY = y + 5 + settingsOffset + 20;

                settingsOffset += setting.draw(settingX, settingY, settingsFont, elementColor, visibleAnim.getValue() * openAnim.getValue());
            }
        } else if (selectedCategory != null) {
            StencilUtils.setUpTexture(x + 100 + 2, y + 2, width - 100 - 4, height - 4, 7.5f);
            StencilUtils.writeTexture();

            moduleScrollTotalHeight = 0;
            float moduleOffset = moduleScroll.getScrollAnim().getValue();
            for (Module module : Modules.getModulesByCategory(selectedCategory)) {
                EasingAnimation toggleAnim = module.getToggleAnimation();
                toggleAnim.update(4, Easing.OUT_CUBIC);
                toggleAnim.setEnd(module.isToggled());

                Colors moduleNameColor = new Colors(clickGui.color.getMixedColor(Modules.getModulesByCategory(selectedCategory).indexOf(module))).withAlphaClamp(openAnim.getValue());
                Colors moduleColor = new Colors(ColorUtils.interpolateColor(elementGrayColor, moduleNameColor, toggleAnim.getValue()));

                RenderUtils.drawRoundedOutLineRectangle(x + 100 + 5, y + 5 + moduleOffset, width - 100 - 10, 25, 7.5f * 1.7f, elementGrayColor.withAlpha(toggleAnim.getValue()).getRGB(), moduleColor.getRGB(), moduleColor.getRGB());
                font.drawString(module.getName(), x + 100 + 5 + 5, y + 5 + 5 + moduleOffset, moduleNameColor);
                catFonts.drawString(module.getDescription(), x + 100 + 5 + 5, y + 5 + 15 + moduleOffset, elementTextColor);

                moduleOffset += 30;
                moduleScrollTotalHeight += 30;
            }

            StencilUtils.endWriteTexture();
        }

        ScaleUtils.stopScaling();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float categoryOffset = 0;
        for (Category category : Category.values()) {
            boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + 10, y + 30 + categoryOffset, 80, 12.5f);

            if (hovered) {
                switch (mouseButton) {
                    case 0 -> selectedCategory = category;
                }
            }

            categoryOffset += 17.5f;
        }

        ClientFont font = Fonts.fonts.get("SFProRegular");
        if (selectedModule != null) {
            float settingsOffset = 0;
            for (Setting setting : selectedModule.getSettings()) {
                EasingAnimation visibleAnim = setting.getVisibleAnim();
                visibleAnim.update(4f, Easing.OUT_CUBIC);
                visibleAnim.setEnd(setting.isVisible());

                if (visibleAnim.getValue() <= 0) continue;

                float settingX = x + 100 + 5;
                float settingY = y + 5 + settingsOffset;

                settingsOffset += setting.mouseClicked(mouseX, mouseY, settingX, settingY, mouseButton, font) * visibleAnim.getValue();
            }
        } else if (selectedCategory != null) {
            boolean moduleListHovered = GuiUtils.isHovered(mouseX, mouseY, x + 100, y + 2, width - 100, height - 4);

            if (moduleListHovered) {
                float moduleOffset = moduleScroll.getScrollAnim().getValue();
                for (Module module : Modules.getModulesByCategory(selectedCategory)) {
                    boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + 100 + 5, y + 5 + moduleOffset, width - 100 - 10, 25f);

                    if (hovered) {
                        switch (mouseButton) {
                            case 0 -> module.toggle();
                            case 1 -> {
                                openSettingsAnim.setEnd(1f);
                                selectedModule = module;
                            }
                        }
                    }
                    moduleOffset += 30;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !closing) {
            openAnim.setEnd(0);
            closing = true;
        }
    }

    private void updateGuiAnimations() {
        moduleScroll.getScrollAnim().update(3f, Easing.OUT_CUBIC);
        openSettingsAnim.update(2, Easing.OUT_BACK);
        openAnim.update(3f, Easing.OUT_CUBIC);
    }
}
