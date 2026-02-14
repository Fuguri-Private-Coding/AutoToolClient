package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.ScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.gui.mouse.MouseDelta;
import fuguriprivatecoding.autotoolrecode.utils.gui.mouse.MousePoint;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.joml.Vector2i;
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

    final CheckBox takeAllLoot = new CheckBox("TakeAllLoot", this, false);

    final CheckBox render = new CheckBox("Render", this, true);
    final MultiMode renderModes = new MultiMode("RenderModes", this, render::isToggled)
        .addModes("Cursor", "HoverSlot");

    final ColorSetting color = new ColorSetting("Color", this, () -> render.isToggled() && renderModes.get("Cursor"));

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch = new StopWatch();
    final StopWatch startDelayStopWatch = new StopWatch();
    final StopWatch closeDelayStopWatch = new StopWatch();

    int startDelayTick, closeDelayTick, lootDelayTick;

    boolean opened = false;

    List<Vector2i> slots = new CopyOnWriteArrayList<>();
    Vector2i currentSlot = new Vector2i(0, 0);

    MousePoint mouse = new MousePoint();

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                String chestName = guiChest.getLowerChestInventory().getDisplayName().getUnformattedText();

                if (!startDelayStopWatch.reachedMS(startDelayTick * 50L) ||
                    checkName.isToggled() && !chestName.contains("Chest")) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

                if (!opened) {
                    opened = true;
                }

                slots = getSlots(container);

                slots.sort(Comparator.comparingInt(slot -> {
                    int deltaX = mouse.getMouseX() - slot.x;
                    int deltaY = mouse.getMouseY() - slot.y;

                    return (int) Math.hypot(deltaX, deltaY);
                }));

                currentSlot = slots.getFirst();

                MouseDelta delta = mouse.getDelta(currentSlot)
                    .limit(this.moveSpeed.getRandomizedIntValue())
                    .divine((float) smooth.getRandomizedDoubleValue());

                if (slots.getFirst().x != -1488) {
                    mouse.move(delta);

                    if (delta.hypot() < minDeltaToClick.getValue()) {
                        if (delayStopWatch.reachedMS(lootDelayTick * 50L) && slots.contains(currentSlot)) {
                            mouse.click();
                        }
                    } else {
                        lootDelayTick = delay.getRandomizedIntValue();
                        delayStopWatch.reset();
                    }

                    if (fail.isToggled() && Math.random() <= failChance.getRandomizedIntValue() / 100f) {
                        mouse.click();
                    }
                }

                if (autoClose.isToggled()) {
                    if (slots.getFirst().x == -1488) {
                        if (closeDelayStopWatch.reachedMS(closeDelayTick * 50L)) {
                            mc.thePlayer.closeScreen();
                        }
                    } else {
                        closeDelayTick = closeDelay.getRandomizedIntValue();
                        closeDelayStopWatch.reset();
                    }
                }
            } else {
                startDelayTick = startDelay.getRandomizedIntValue();
                startDelayStopWatch.reset();
                mouse.reset();
                opened = false;
            }
        }

        if (slots.getFirst().x != 1488 && opened && render.isToggled()) {
            if (event instanceof ScreenEvent e) {
                if (e.getType() == ScreenEvent.Type.PRE && renderModes.get("HoverSlot")) {
                    e.cancel();
                    mouse.render();
                } else {
                    if (renderModes.get("Cursor")) {
                        float x = mouse.getPrevX() + (mouse.getMouseX() - mouse.getPrevX()) * mc.timer.renderPartialTicks;
                        float y = mouse.getPrevY() + (mouse.getMouseY() - mouse.getPrevY()) * mc.timer.renderPartialTicks;

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
            if (!InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i)) && !takeAllLoot.isToggled())
                continue;

            final Slot slot = container.getSlot(i);
            if (slot.getHasStack()) {
                GuiContainer cont = (GuiContainer) mc.currentScreen;

                int slotCenterX = cont.getGuiLeft() + slot.xDisplayPosition + 8;
                int slotCenterY = cont.getGuiTop() + slot.yDisplayPosition + 8;

                slots.add(new Vector2i(slotCenterX, slotCenterY));
            }
        }

        return slots;
    }
}