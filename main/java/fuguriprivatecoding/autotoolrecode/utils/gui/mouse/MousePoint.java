package fuguriprivatecoding.autotoolrecode.utils.gui.mouse;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.joml.Vector2i;

@Getter
@Setter
public class MousePoint implements Imports {
    int mouseX, prevX;
    int mouseY, prevY;

    public void move(MouseDelta delta) {
        prevUpdate();
        mouseX += delta.deltaX;
        mouseY += delta.deltaY;
    }

    public void click() {
        try {
            mc.currentScreen.mouseClick(mouseX, mouseY, 1);
        } catch (Exception ignored) {}
    }

    public void render() {
        mc.currentScreen.drawScreen(mouseX, mouseY, mc.timer.renderPartialTicks);
    }

    public void reset() {
        ScaledResolution sc = new ScaledResolution(mc);
        prevUpdate();
        mouseX = sc.getScaledWidth() / 2;
        mouseY = sc.getScaledHeight() / 2;
    }

    public MouseDelta deltaTo(Vector2i end) {
        return new MouseDelta(end.x - mouseX, end.y - mouseY);
    }

    private void prevUpdate() {
        prevX = mouseX;
        prevY = mouseY;
    }
}
