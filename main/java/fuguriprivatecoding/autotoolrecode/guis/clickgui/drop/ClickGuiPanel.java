package fuguriprivatecoding.autotoolrecode.guis.clickgui.drop;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.scaling.ScaleUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.util.List;

public class ClickGuiPanel {

    public ClickGuiPanel(Category category, float initialX, float initialY) {
        this.category = category;
        this.xPosAnim.setValue(initialX);
        this.yPosAnim.setValue(initialY);
        modules = Client.INST.getModuleManager().getModulesByCategory(category);
    }

    private final List<Module> modules;

    private final Category category;

    boolean opened, moving;

    EasingAnimation xPosAnim = new EasingAnimation();
    EasingAnimation yPosAnim = new EasingAnimation();

    EasingAnimation heightAnim = new EasingAnimation();
    EasingAnimation heightAnimNormalized = new EasingAnimation();

    EasingAnimation openSettingsAnim = new EasingAnimation();

    private Module openedModule, lastOpenedModule;

    private float totalElementsHeight;

    private final Vector2f moveOffset = new Vector2f();

    EasingAnimation scrollAnim = new EasingAnimation();

    int scrollBeforeOpenSettings;
    int scroll;

    float backgroundX;
    float backgroundY;
    float backgroundWidth;
    float backgroundHeight;

    float panelHeaderHeight = 25;

    float xOffset = 5;
    float yOffset = panelHeaderHeight;

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

    ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("SFPro");

    public void render(float openAnimProgress, int mouseX, int mouseY) {
        if (GuiUtils.isHovered(mouseX, mouseY, backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
            int currentScroll = Mouse.getDWheel();
            scroll += currentScroll / 120 * 25;
        }

        float altVisibleHeight = 250;
        float maxScroll = Math.max(totalElementsHeight - altVisibleHeight, 0);

        scroll = (int) Math.clamp(scroll, -maxScroll, 0);

        scrollAnim.update(5, Easing.OUT_CUBIC);
        scrollAnim.setEnd(scroll);

        if (moving) {
            xPosAnim.setEnd(mouseX - moveOffset.x);
            yPosAnim.setEnd(mouseY - moveOffset.y);
        }

        ScaledResolution sc = ScaleUtils.getScaledResolution();

        xPosAnim.update(8f, Easing.OUT_CUBIC);
        yPosAnim.update(8f, Easing.OUT_CUBIC);

        heightAnim.setEnd(opened ? Math.min(totalElementsHeight, 250) : 0);
        heightAnim.update(3, Easing.IN_OUT_CUBIC);

        heightAnimNormalized.setEnd(opened ? 1 : 0);
        heightAnimNormalized.update(3, Easing.IN_OUT_CUBIC);


        openSettingsAnim.update(5, Easing.IN_OUT_CUBIC);

        float invertOpenAnimProgress = 1 - openAnimProgress;
        float panelRadius = 5;

        Color panelColor = new Color(0.1f,0.1f,0.1f, 0.7f);

        Color[] panelOutLineColors = new Color[] {
            ColorUtils.fadeColor(clickGui.color.getColor(), clickGui.color.getFadeColor(), clickGui.color.getSpeed()),
            ColorUtils.fadeColor(clickGui.color.getFadeColor(), clickGui.color.getColor(), clickGui.color.getSpeed())
        };

        backgroundX = xPosAnim.getValue() - invertOpenAnimProgress * 10 / 2;
        backgroundY = yPosAnim.getValue() - invertOpenAnimProgress * 10 / 2;

        backgroundWidth = 100 + invertOpenAnimProgress * 10;
        backgroundHeight = 20 + heightAnim.getValue() + invertOpenAnimProgress * 10;

        float invertHeightAnim = 1 - heightAnimNormalized.getValue();

        RoundedUtils.drawRect(
            backgroundX, backgroundY,
            backgroundWidth, backgroundHeight,
            panelRadius,
            panelColor
        );

        RoundedUtils.drawRect(
            backgroundX,backgroundY,
            backgroundWidth, 20,
            5 * invertHeightAnim, 5,5,5 * invertHeightAnim,
            panelColor.darker()
        );

        fontRenderer.drawCenteredString(
            category.name,
            backgroundX + backgroundWidth / 2f,
            backgroundY + 8,
            Color.WHITE
        );

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(
            sc,
            backgroundX, backgroundY + 20,
            backgroundWidth, backgroundHeight - 20
        );

        if (heightAnimNormalized.getValue() > 0) {
            totalElementsHeight = 0;

            for (Module module : modules) {
                float moduleX = backgroundX + xOffset + openSettingsAnim.getValue() * backgroundWidth;
                float moduleY = backgroundY + yOffset + totalElementsHeight + scrollAnim.getValue();
                float moduleHeight = 20;
                float moduleWidth = backgroundWidth - xOffset * 2;

                boolean hovered = GuiUtils.isHovered(mouseX, mouseY, moduleX, moduleY, moduleWidth, moduleHeight);

                Color baseColor = new Color(0f, 0f, 0f, 0.7f);

                if (hovered) {
                    baseColor = new Color(baseColor.getRed() / 255f + 0.1f, baseColor.getGreen() / 255f + 0.1f, baseColor.getBlue() / 255f + 0.1f, 0.7f);
                }

                if (module.isToggled()) {
                    baseColor = new Color(baseColor.getRed() / 255f + 0.2f, baseColor.getGreen() / 255f + 0.2f, baseColor.getBlue() / 255f + 0.2f, 0.7f);
                }

                RoundedUtils.drawRect(moduleX, moduleY, moduleWidth, moduleHeight, 5f, baseColor);
                fontRenderer.drawString(module.getName(), moduleX + 5, moduleY + 8, Color.WHITE);

                totalElementsHeight += 25;
            }

            if (openSettingsAnim.getValue() > 0) {
                float totalSettingsHeight = 0;
                for (Setting setting : lastOpenedModule.getSettings()) {
                    float settingX = backgroundX + xOffset + (1 - openSettingsAnim.getValue()) * backgroundWidth;
                    float settingY = backgroundY + yOffset + totalSettingsHeight + scrollAnim.getValue();

                    switch (setting) {
                        case FloatSetting floatSetting -> {
                            fontRenderer.drawString(
                                floatSetting.getName() + " " + floatSetting.getAnimatedValue(),
                                settingX, settingY,
                                Color.WHITE
                            );
                            totalSettingsHeight += 20;
                        }
                        default -> {
                        }
                    }
                }

                totalElementsHeight = Math.max(totalElementsHeight, totalSettingsHeight);
            }

            totalElementsHeight += 5;
        }

        ScissorUtils.disableScissor();

    }

    public boolean onMouse(int x, int y, int button) {
        if (GuiUtils.isHovered(x, y, backgroundX, backgroundY, backgroundWidth, panelHeaderHeight)) {
            switch (button) {
                case 0 -> {
                    moveOffset.set(x - backgroundX,y - backgroundY);
                    moving = true;
                    return true;
                }
                case 1 -> {
                    opened = !opened;
                    return true;
                }
            }
        }

        if (!opened) return false;

        float totalElementHeight = 0;
        for (Module module : modules) {
            float moduleX = backgroundX + xOffset;
            float moduleY = backgroundY + yOffset + totalElementHeight + scrollAnim.getValue();
            float moduleWidth = backgroundWidth - xOffset * 2;
            float moduleHeight = 20;

            boolean hovered = GuiUtils.isHovered(x, y, moduleX, moduleY, moduleWidth, moduleHeight);

            if (hovered) {
                switch (button) {
                    case 0 -> module.toggle();
                    case 1 -> {
                        if (!module.getSettings().isEmpty()) {
                            openedModule = module;
                            lastOpenedModule = module;
                            scrollBeforeOpenSettings = (int) scrollAnim.getValue();
                            scroll = 0;
                            openSettingsAnim.setEnd(1);
                        }
                    }
                }
                return true;
            }

            totalElementHeight += 25;
        }
        totalElementHeight += 5;


        return false;
    }

    public void onMouseRelease(int x, int y, int button) {
        moving = false;
    }

    public boolean keyTyped(char ch, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            boolean aa = openedModule != null;
            openedModule = null;
            scroll = scrollBeforeOpenSettings;
            openSettingsAnim.setEnd(0);
            return aa;
        }

        return false;
    }

}
