package fuguriprivatecoding.autotoolrecode.guis.clickgui.drop;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropDownClickGuiScreen extends GuiScreen {

    private final ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);
    private final List<ClickGuiPanel> categoryPanels = new ArrayList<>();
    private final EasingAnimation openAnim = new EasingAnimation();

    private boolean closing;

    public DropDownClickGuiScreen() {
        float offset = 0;

        for (Category value : Category.values()) {
            categoryPanels.add(new ClickGuiPanel(value, 0, offset));
            offset += 30;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        openAnim.update(clickGui.animationSpeed.getValue(), Easing.IN_OUT_CUBIC);

        if (closing) {
            if (!openAnim.isAnimating()) {
                mc.displayGuiScreen(null);
                closing = false;
            }

            mouseX = 0;
            mouseY = 0;
        }

        AlphaUtils.startWrite();

        for (ClickGuiPanel panel : categoryPanels.reversed()) {
            panel.render(openAnim.getValue(), mouseX, mouseY);
        }

        AlphaUtils.endWrite();
        AlphaUtils.draw(openAnim.getValue());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (ClickGuiPanel panel : categoryPanels) {
            if (panel.onMouse(mouseX, mouseY, mouseButton)) {
                break;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (ClickGuiPanel panel : categoryPanels) {
            panel.onMouseRelease(mouseX, mouseY, state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        openAnim.setValue(0);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (ClickGuiPanel categoryPanel : categoryPanels) {
            if (categoryPanel.keyTyped(typedChar, keyCode)) {
                return;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            closing = true;
            openAnim.setEnd(0);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }




}
