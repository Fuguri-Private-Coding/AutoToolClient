package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.ScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.joml.Vector2i;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 20, 5, 1);

    final DoubleSlider moveSpeed = new DoubleSlider("MoveSpeed", this, 0, 100, 50, 1);
    final DoubleSlider smooth = new DoubleSlider("Smooth", this, 1, 3, 2, 0.1f);
    final FloatSetting minDeltaToClick = new FloatSetting("MinDeltaToClick", this, 0, 10, 3, 0.1f);

    final CheckBox fail = new CheckBox("Fail", this, false);
    final DoubleSlider failChance = new DoubleSlider("FailChance", this, fail::isToggled, 0, 100, 20, 1);

    final DoubleSlider delay = new DoubleSlider("Delay", this, 0,10,1,1);

    final CheckBox autoClose = new CheckBox("AutoClose", this, true);
    final DoubleSlider closeDelay = new DoubleSlider("CloseDelay", this, autoClose::isToggled, 0,10,1,1);

    final CheckBox anyLoot = new CheckBox("AnyLoot", this, false);

    final CheckBox render = new CheckBox("Render", this, true);
    final MultiMode renderModes = new MultiMode("RenderModes", this, render::isToggled)
        .addModes("Cursor", "HoverSlot");

    final ColorSetting color = new ColorSetting("Color", this, () -> render.isToggled() && renderModes.get("Cursor"));

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch = new StopWatch();
    final StopWatch startDelayStopWatch = new StopWatch();
    final StopWatch closeDelayStopWatch = new StopWatch();

    boolean opened = false;
    long delays;

    List<Vector2i> slots = new CopyOnWriteArrayList<>();
    Vector2i currentSlot = new Vector2i(0, 0);

    MousePoint mouse = new MousePoint();

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                String chestName = guiChest.getLowerChestInventory().getDisplayName().getUnformattedText();

                if (checkName.isToggled()) {
                    if (!chestName.contains("Chest")) return;
                }

                if (!startDelayStopWatch.reachedMS(startDelay.getRandomizedIntValue() * 50L)) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

                if (!opened) {
                    slots = getSlots(container);
                    opened = true;
                }

                slots.sort(Comparator.comparingInt(slot -> {
                    int dx = mouse.mouseX - slot.x;
                    int dy = mouse.mouseY - slot.y;

                    return (int) Math.hypot(dx, dy);
                }));

                currentSlot = slots.getFirst();

                int moveSpeed = this.moveSpeed.getRandomizedIntValue();

                int deltaX = (int) (Math.clamp(currentSlot.x - mouse.mouseX, -moveSpeed, moveSpeed) / smooth.getRandomizedDoubleValue());
                int deltaY = (int) (Math.clamp(currentSlot.y - mouse.mouseY, -moveSpeed, moveSpeed) / smooth.getRandomizedDoubleValue());

                if (slots.getFirst().x != -1488) {
                    mouse.move(deltaX, deltaY);

                    if (Math.hypot(deltaX, deltaY) < minDeltaToClick.getValue()) {
                        if (delayStopWatch.reachedMS(delays) && slots.contains(currentSlot)) {
                            mouse.click();

                            delayStopWatch.reset();
                            slots.remove(currentSlot);
                        }
                    } else {
                        delays = delay.getRandomizedIntValue() * 50L;
                    }

                    if (fail.isToggled() && Math.random() <= failChance.getRandomizedIntValue() / 100f) {
                        mouse.click();
                    }
                }

                if (autoClose.isToggled()) {
                    if (slots.getFirst().x == -1488) {
                        if (closeDelayStopWatch.reachedMS(closeDelay.getRandomizedIntValue() * 50L)) {
                            mc.thePlayer.closeScreen();
                        }
                    } else {
                        closeDelayStopWatch.reset();
                    }
                }
            } else {
                startDelayStopWatch.reset();
                opened = false;
                mouse.reset();
            }
        }

        if (slots.getFirst().x != 1488 && opened && render.isToggled()) {
            if (event instanceof ScreenEvent e) {
                if (e.getType() == ScreenEvent.Type.PRE && renderModes.get("HoverSlot")) {
                    e.cancel();
                    mouse.render();
                } else {
                    if (renderModes.get("Cursor")) {
                        float x = mouse.prevX + (mouse.mouseX - mouse.prevX) * mc.timer.renderPartialTicks;
                        float y = mouse.prevY + (mouse.mouseY - mouse.prevY) * mc.timer.renderPartialTicks;

                        ColorUtils.glColor(color.getFadedColor());
                        RenderUtils.drawImage(Client.INST.of("image/cursor.png"), x - 2, y, 10, 10, true);
                    }
                }
            }
        }
    }

    List<Vector2i> getSlots(ContainerChest container) {
        List<Vector2i> slots = new CopyOnWriteArrayList<>();

        slots.add(new Vector2i(-1488, -1488));

        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            final Slot slot = container.getSlot(i);
            if (!InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i)) && !anyLoot.isToggled())
                continue;

            if (slot.getHasStack()) {
                GuiContainer cont = (GuiContainer) mc.currentScreen;

                int slotCenterX = cont.getGuiLeft() + slot.xDisplayPosition + 8;
                int slotCenterY = cont.getGuiTop() + slot.yDisplayPosition + 8;

                slots.add(new Vector2i(slotCenterX, slotCenterY));
            }
        }

        return slots;
    }

    private static class MousePoint {
        int mouseX, prevX;
        int mouseY, prevY;

        public void move(int deltaX, int deltaY) {
            prevUpdate();
            mouseX += deltaX;
            mouseY += deltaY;
        }

        public void click() {
            try {
                GuiContainer.forceShift = true;
                mc.currentScreen.mouseClick(mouseX, mouseY, 1);
                GuiContainer.forceShift = false;
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

        private void prevUpdate() {
            prevX = mouseX;
            prevY = mouseY;
        }
    }
}