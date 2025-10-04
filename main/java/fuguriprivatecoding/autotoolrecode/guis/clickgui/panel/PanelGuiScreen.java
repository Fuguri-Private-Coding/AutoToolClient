package fuguriprivatecoding.autotoolrecode.guis.clickgui.panel;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.scaling.ScaleUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PanelGuiScreen extends GuiScreen {

    List<Panel> modulePanels = new CopyOnWriteArrayList<>();

    Category selectedCategory;

    EasingAnimation openAnim = new EasingAnimation();

    boolean opened;

    float backgroundX, backgroundY, backgroundWidth, backgroundHeight;

    float categoryPanelWidth;

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

    ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("SFProRegular");

    EasingAnimation modulesScrollAnim = new EasingAnimation();
    int modulesScroll;

    float moduleOffset;


    {
        backgroundX = 5;
        backgroundY = 5;
        backgroundWidth = 150;
        backgroundHeight = ScaleUtils.getScaledResolution().scaledHeight - 10;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int currentScroll = Mouse.getDWheel();
        ScaledResolution sc = new ScaledResolution(mc);

        openAnim.update(3, Easing.IN_OUT_QUINT);
        openAnim.setEnd(opened ? 1 : 0);

        fontRenderer = Client.INST.getFonts().fonts.get("SFPro");

        opened = GuiUtils.isHovered(mouseX, mouseY, 0, 0, 150 + 5, backgroundHeight + 10);

        float invertOpenAnim = 1 - openAnim.getValue();

        backgroundX = -150 + openAnim.getValue() * 155;

        backgroundWidth = 155 * openAnim.getValue();
        backgroundHeight = sc.getScaledHeight() - 10;

        if (GuiUtils.isHovered(mouseX, mouseY, backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
            modulesScroll += currentScroll / 120 * 25;
        }

        float altVisibleHeight = backgroundHeight;
        float maxScroll = Math.max(moduleOffset - altVisibleHeight, 0);
        modulesScroll = (int) Math.clamp(modulesScroll, -maxScroll, 0);

        modulesScrollAnim.update(4f, Easing.OUT_CUBIC);
        modulesScrollAnim.setEnd(modulesScroll);

        Color panelColor = new Color(0,0,0, clickGui.backgroundAlpha.getValue());
        float panelRadius = 10;

        AlphaUtils.startWrite();
        RoundedUtils.drawRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight, panelRadius, panelColor);

        categoryPanelWidth = 50 * openAnim.getValue();

        RoundedUtils.drawRect(backgroundX, backgroundY, categoryPanelWidth, backgroundHeight, 10,10,0,0, panelColor);

        float finalCategoryOffset = backgroundHeight / Category.values().length;

        float categoryOffset = 10;
        for (Category category : Category.values()) {
            float categoryX = (float) (backgroundX + categoryPanelWidth / 2f - fontRenderer.getStringWidth(category.name) / 2f);
            float categoryY = backgroundY + 5 + categoryOffset;
            float categoryWidth = (float) fontRenderer.getStringWidth(category.name);

            boolean selected = selectedCategory != null && selectedCategory.equals(category);

            fontRenderer.drawString(category.name, categoryX, categoryY, selected ? clickGui.color.getFadedColor() : Color.WHITE);

            categoryOffset += finalCategoryOffset;
        }

        List<Module> moduleList = Client.INST.getModuleManager().getModulesByCategory(selectedCategory);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, backgroundX, backgroundY + 2, backgroundWidth, backgroundHeight - 4);

        moduleOffset = modulesScrollAnim.getValue();
        for (Module module : moduleList) {
            float moduleX = backgroundX + categoryPanelWidth + 5;
            float moduleY = backgroundY + 5 + moduleOffset;
            float moduleWidth = 95;
            float moduleHeight = 25;

            boolean hovered = GuiUtils.isHovered(mouseX, mouseY, moduleX, moduleY, moduleWidth, moduleHeight);

            Color baseColor = getBaseColor(module, hovered);

            RoundedUtils.drawRect(moduleX, moduleY, moduleWidth, moduleHeight, 10, baseColor);
            fontRenderer.drawString(module.getName(), moduleX + 5, moduleY + 10.5f, Color.WHITE);
            moduleOffset += 30;
        }

        ScissorUtils.disableScissor();

        AlphaUtils.endWrite();
        AlphaUtils.draw(openAnim.getValue() * 1.7f);

        for (Panel panel : modulePanels) {
            panel.render(mouseX, mouseY, currentScroll);

            modulePanels.removeIf(panel1 -> panel1.closed);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private Color getBaseColor(Module module, boolean hovered) {
        Color baseColor = new Color(0f, 0f, 0f, clickGui.backgroundAlpha.getValue() / 255f);

        if (hovered) {
            baseColor = new Color(baseColor.getRed() / 255f + 0.1f, baseColor.getGreen() / 255f + 0.1f, baseColor.getBlue() / 255f + 0.1f, clickGui.backgroundAlpha.getValue() / 255f);
        }

        if (module.isToggled()) {
            baseColor = new Color(baseColor.getRed() / 255f + 0.2f, baseColor.getGreen() / 255f + 0.2f, baseColor.getBlue() / 255f + 0.2f, clickGui.backgroundAlpha.getValue() / 255f);
        }
        return baseColor;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float finalCategoryOffset = backgroundHeight / Category.values().length;

        float categoryOffset = 10;
        for (Category category : Category.values()) {
            float categoryX = (float) (backgroundX + categoryPanelWidth / 2f - fontRenderer.getStringWidth(category.name) / 2f);
            float categoryY = backgroundY + 5 + categoryOffset;
            float categoryWidth = (float) fontRenderer.getStringWidth(category.name);

            if (GuiUtils.isHovered(mouseX, mouseY, categoryX, categoryY, categoryWidth, fontRenderer.FONT_HEIGHT) && mouseButton == 0) {
                selectedCategory = category;
            }

            categoryOffset += finalCategoryOffset;
        }

        List<Module> moduleList = Client.INST.getModuleManager().getModulesByCategory(selectedCategory);

        moduleOffset = modulesScrollAnim.getValue();
        for (Module module : moduleList) {
            float moduleX = backgroundX + categoryPanelWidth + 5;
            float moduleY = backgroundY + 5 + moduleOffset;
            float moduleWidth = 95;
            float moduleHeight = 25;

            if (GuiUtils.isHovered(mouseX, mouseY, moduleX, moduleY, moduleWidth, moduleHeight)) {
                switch (mouseButton) {
                    case 0 -> module.toggle();
                    case 1 -> modulePanels.add(new Panel(module, moduleX, moduleY, moduleWidth));
                }
            }

            moduleOffset += 30;
        }

        for (Panel panel : modulePanels) {
            if (panel.clickMouse(mouseX,mouseY,mouseButton)) {
                break;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : modulePanels) {
            if (panel.mouseReleased(mouseX,mouseY,state)) {
                break;
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }




}
