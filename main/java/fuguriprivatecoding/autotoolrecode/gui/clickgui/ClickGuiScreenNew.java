package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.Scroll;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ClickGuiScreenNew extends GuiScreen {

    public static ClickGuiScreenNew INST;

    private final EasingAnimation openAnim = new EasingAnimation();
    private final EasingAnimation categorySwitchAnim = new EasingAnimation();

    private final ClickGui clickGui = Modules.getModule(ClickGui.class);

    private float x, y, width, height;
    private boolean binding, closing;

    Category selectedCategory = Category.COMBAT;

    private final Scroll moduleScroll = new Scroll(25);
    private final Scroll settingsScroll = new Scroll(15);

    public static void init() {
        INST = new ClickGuiScreenNew();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);
        x = 50;
        y = 50;
        width = sc.getScaledWidth() - 100;
        height = sc.getScaledHeight() - 100;
        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        openAnim.update(4f, Easing.OUT_CUBIC);

        ClientFont font = Fonts.fonts.get(clickGui.fonts.getMode());

        float alpha = openAnim.getClampValue();

        Color rectColor = Colors.BLACK.withAlphaClamp(0.5f).withMultiplyAlpha(alpha);
        Color rectOutLineColor = Colors.WHITE.withAlphaClamp(0.7f).withMultiplyAlpha(alpha);
        Color textColor = Colors.WHITE.withAlpha(alpha);

        if (closing && openAnim.getValue() == 0.2) {
            mc.currentScreen.onGuiClosed();
            mc.thePlayer.closeScreen();
        }

        RoundedUtils.drawRect(x, y, width, height, 10f, rectColor);
        RoundedUtils.drawRect(x, y, width, 35, 0, 10, 10, 0, rectColor);

        Profile profile = Client.INST.getProfile();

        float profileRectX = x + 5;
        float profileRectY = y + 5;
        float userWidth = font.getStringWidth("User: " + profile.getUsername());
        float roleWidth = font.getStringWidth("Role: " + profile.getRole());

        float profileRectWidth = Math.max(userWidth, roleWidth) + 10;

        RenderUtils.drawRoundedOutLineRectangle(profileRectX, profileRectY, profileRectWidth, 25, 5, rectColor, rectOutLineColor, rectOutLineColor);

        float profileX = profileRectX + 5;
        float profileY = profileRectY + 5;

        font.drawString("User: " + profile.getUsername(), profileX, profileY, textColor);
        font.drawString("Role: " + profile.getRole().getColorPrefix() + profile.getRole(), profileX, profileY + 10, textColor);

        float categoryX = profileRectX + profileRectWidth + 5;
        float categoryY = y + 5;

        float categoryWidth = width - (categoryX - x) - 5;

        RenderUtils.drawRoundedOutLineRectangle(categoryX, categoryY, categoryWidth, 25, 5, rectColor, rectOutLineColor, rectOutLineColor);

        float categoriesX = categoryX + 10;
        float categoriesY = categoryY + 10;

        float categoriesOffset = 0;
        for (Category category : Category.values()) {
            String name = category.name;

            float nameWidth = font.getStringWidth(name);

            boolean selected = selectedCategory == category;
            boolean hovered = GuiUtils.isHovered(mouseX, mouseY, categoriesX + categoriesOffset, categoryY, nameWidth, 25);

            EasingAnimation categoryAnim = category.toggleAnim;

            categoryAnim.update(5, Easing.OUT_CUBIC);
            categoryAnim.setEnd(selected);

            Color categoryDefaultColor = textColor;
            Color categorySelectedColor = new Colors(clickGui.color.getFadedColor()).withAlpha(alpha);
            Color categoryColor = ColorUtils.interpolateColor(categoryDefaultColor, categorySelectedColor.darker(), categoryAnim.getValue());

            font.drawString(name, categoriesX + categoriesOffset, categoriesY, hovered ? categoryColor.darker() : categoryColor);
            categoriesOffset += nameWidth + 7.5f;
        }

        float categoryModulesX = x + 5;
        float categoryModulesY = y + 40;

        categorySwitchAnim.update(2f, Easing.IN_OUT_QUINT);

        StencilUtils.setUpTexture(x, y + 35, width, height - 25, 10, 0, 0, 10);
        StencilUtils.writeTexture();

        float modulesOffsetX = -categorySwitchAnim.getValue();
        for (Category category : Category.values()) {
            List<Module> moduleList = Modules.getModulesByCategory(category);

            float offsetX = 0;
            float offsetY = 0;
            for (Module module : moduleList) {
                float moduleX = categoryModulesX + modulesOffsetX + offsetX;
                float moduleY = categoryModulesY + offsetY;
                float moduleWidth = 75;

                


                if (moduleX <= x + width && moduleX + moduleWidth >= x) {
                    RenderUtils.drawRoundedOutLineRectangle(moduleX, moduleY, moduleWidth, 25, 7.5f, rectColor, rectOutLineColor, rectOutLineColor);
                    font.drawString(module.getName(), moduleX + 5, moduleY + 10, textColor);
                }

                offsetX += moduleWidth + 5;

                if (offsetX + moduleWidth >= width - 10) {
                    offsetX = 0;
                    offsetY += 30;
                }
            }
            modulesOffsetX += width;
        }
        StencilUtils.endWriteTexture();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ClientFont font = Fonts.fonts.get(clickGui.fonts.getMode());

        Profile profile = Client.INST.getProfile();

        float profileRectX = x + 5;
        float userWidth = font.getStringWidth("User: " + profile.getUsername());
        float roleWidth = font.getStringWidth("Role: " + profile.getRole());

        float profileRectWidth = Math.max(userWidth, roleWidth) + 10;

        float categoryX = profileRectX + profileRectWidth + 5;
        float categoryY = y + 5;

        float categoriesX = categoryX + 10;
        float categoriesY = categoryY + 10;

        float categoriesOffset = 0;
        for (Category category : Category.values()) {
            String name = category.name;

            float nameWidth = font.getStringWidth(name);

            boolean clicked = GuiUtils.isHovered(mouseX, mouseY, categoriesX + categoriesOffset, categoryY, nameWidth, 25);

            if (clicked && selectedCategory != category) {
                selectedCategory = category;
                categorySwitchAnim.setEnd(width * selectedCategory.ordinal());
            }

            categoriesOffset += nameWidth + 7.5f;
        }
    }
}
