package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.HUDElement;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import java.util.List;

@ModuleInfo(name = "HUD", category = Category.VISUAL, description = "Добавляет на экран полезную информацию.")
public class HUD extends Module {

    HUDElement selectedHudElement;
    boolean draggingElement;
    Vector2f lastMouse = new Vector2f(0, 0);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            int screenWidth = e.getWidth();
            int screenHeight = e.getHeight();
            int mouseX = e.getMouseX();
            int mouseY = e.getMouseY();

            if (Client.INST.getHudManager() == null) return;

            List<HUDElement> hudElements = Client.INST.getHudManager().hudElements;

            for (HUDElement hudElement : hudElements) {
                if (mc.currentScreen instanceof GuiChat) {
                    if (isDraggingElement(hudElement, mouseX, mouseY)) {
                        selectedHudElement = hudElement;
                        lastMouse.set(mouseX, mouseY);
                        draggingElement = true;
                    }

                    if (draggingElement && Mouse.isButtonDown(0) && selectedHudElement.equals(hudElement)) {
                        float deltaX = mouseX - lastMouse.x;
                        float deltaY = mouseY - lastMouse.y;

                        if (Math.abs(deltaX) >= 0.5f || Math.abs(deltaY) >= 0.5f) {
                            hudElement.getPos().x += deltaX;
                            hudElement.getPos().y += deltaY;

                            float elemW = hudElement.getSize().x;
                            float elemH = hudElement.getSize().y;

                            hudElement.getPos().endX = Math.max(0, Math.min(hudElement.getPos().x, screenWidth - elemW));
                            hudElement.getPos().endY = Math.max(0, Math.min(hudElement.getPos().y, screenHeight - elemH));

                            lastMouse.set(mouseX, mouseY);
                        }
                    } else if (!Mouse.isButtonDown(0)) {
                        selectedHudElement = null;
                        draggingElement = false;
                    }
                }

                hudElement.render();
            }
        }
    }

    public boolean isDraggingElement(HUDElement hudElement, int mouseX, int mouseY) {
        return (Mouse.isButtonDown(0) && !draggingElement && mouseX > hudElement.getPos().x
                && mouseX < hudElement.getPos().x + hudElement.getSize().x
                && mouseY > hudElement.getPos().y && mouseY < hudElement.getPos().y + hudElement.getSize().y);
    }
}