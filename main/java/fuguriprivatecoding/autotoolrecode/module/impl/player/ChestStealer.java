package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.GuiScreen;
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

    final CheckBox render = new CheckBox("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this, render::isToggled);

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

                mouse.move(deltaX, deltaY);

                if (slots.getFirst().x != -1488) {
                    if (Math.hypot(deltaX, deltaY) < minDeltaToClick.getValue()) {
                        if (delayStopWatch.reachedMS(delays) && slots.contains(currentSlot)) {
                            mouse.click(mc.currentScreen);

                            delayStopWatch.reset();
                            slots.remove(currentSlot);
                        }
                    } else {
                        delays = delay.getRandomizedIntValue() * 50L;
                    }

                    if (fail.isToggled() && Math.random() <= failChance.getRandomizedIntValue() / 100f) {
                        mouse.click(mc.currentScreen);
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

        if (event instanceof RenderScreenEvent && opened && render.isToggled()) {
            RoundedUtils.drawRect(mouse.mouseX - 1, mouse.mouseY - 1, 2, 2, 1, color.getFadedColor());
        }
    }

    List<Vector2i> getSlots(ContainerChest container) {
        List<Vector2i> slots = new CopyOnWriteArrayList<>();

        slots.add(new Vector2i(-1488, -1488));

        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            final Slot slot = container.getSlot(i);
            if (InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
                if (slot.getHasStack()) {
                    GuiContainer cont = (GuiContainer) mc.currentScreen;

                    int slotCenterX = cont.getGuiLeft() + slot.xDisplayPosition + 8;
                    int slotCenterY = cont.getGuiTop() + slot.yDisplayPosition + 8;

                    slots.add(new Vector2i(slotCenterX, slotCenterY));
                }
            }
        }

        return slots;
    }

    private static class MousePoint {
        int mouseX;
        int mouseY;

        public void move(int deltaX, int deltaY) {
            mouseX += deltaX;
            mouseY += deltaY;
        }

        public void click(GuiScreen guiScreen) {
            try {
                GuiContainer.forceShift = true;
                guiScreen.mouseClick(mouseX, mouseY, 1);
                GuiContainer.forceShift = false;
            } catch (Exception ignored) {}
        }

        public void reset() {
            ScaledResolution sc = new ScaledResolution(mc);
            mouseX = sc.getScaledWidth() / 2;
            mouseY = sc.getScaledHeight() / 2;
        }
    }
}