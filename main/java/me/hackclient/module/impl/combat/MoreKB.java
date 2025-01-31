package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.w3c.dom.Entity;

@ModuleInfo(
        name = "MoreKB",
        category = Category.COMBAT
)
public class MoreKB extends Module {

    final StopWatch stopWatch;

    final IntegerSetting delayTime = new IntegerSetting("Delay", this, 0, 500, 300);

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "LegitFast",
            new String[] {
                    "Legit",
                    "LegitBack",
                    "LegitFast",
                    "LegitSneak",
                    "LegitBlock",
                    "Custom"
            }
    );

    final IntegerSetting minDelay = new IntegerSetting("MinTickDelayAfterHit", this, 0, 10, 3);
    final IntegerSetting maxDelay = new IntegerSetting("MaxTickDelayAfterHit", this, 0, 10, 3);
    final IntegerSetting minReset = new IntegerSetting("MinTickResetDuration", this, 0, 10, 1);
    final IntegerSetting maxReset = new IntegerSetting("MaxTickResetDuration", this, 0, 10, 1);

    final ModeSetting customEventSettings = new ModeSetting(
            "CustomEventMode",
            this,
            () -> mode.getMode().equals("Custom"),
            "Tick",
            new String[]{
                    "Tick",
                    "Sprint",
                    "Update"
            }
    );

    final MultiBooleanSetting customSettings = new MultiBooleanSetting("CustomModes", this, () -> mode.getMode().equals("Custom"))
            .add("CancelSprint")
            .add("CancelServerSprint")
            .add("Packet STOP_SPRINTING")
            .add("Packet START_SPRINTING");


    public MoreKB() {
        stopWatch = new StopWatch();
    }

    int delay, reset;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

        if (event instanceof AttackEvent && stopWatch.reachedMS() >= delayTime.getValue()) {
            stopWatch.reset();
            delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
            reset = RandomUtils.nextInt(minReset.getValue(), maxReset.getValue());
        }

        if (delay > 0) {
            if (event instanceof TickEvent) delay--;
            return;
        }

        if (reset == 0) return;

        switch (mode.getMode()) {
            case "Custom" -> {
                if (!mc.thePlayer.isSprinting()) break;
                switch (customEventSettings.getMode()) {
                    case "Tick" -> {
                        if (event instanceof TickEvent) {
                            handleCustomReset();
                        }
                    }
                    case "Sprint" -> {
                        if (event instanceof SprintEvent) {
                            handleCustomReset();
                        }
                    }
                    case "Update" -> {
                        if (event instanceof UpdateEvent) {
                            handleCustomReset();
                        }
                    }
                }
            }

            case "Legit" -> {
                if (event instanceof MoveEvent moveEvent) {
                    moveEvent.setForward(0f);
                }
            }

            case "LegitBack" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setForward(false);
                    moveButtonEvent.setBack(true);
                }
            }

            case "LegitFast" -> {
                if (event instanceof TickEvent) {
                    mc.thePlayer.test = true;
                }
            }

            case "LegitSneak" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setSneak(true);
                }
            }

            case "LegitBlock" -> {
                if (mc.thePlayer.getHeldItem() == null)
                    break;

                if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))
                    break;

                if (event instanceof RunGameLoopEvent) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                }
            }
        }

        if (event instanceof TickEvent && reset > 0) {
            reset--;
        }
    }

    void handleCustomReset() {
        if (customSettings.get("CancelSprint")) mc.thePlayer.setSprinting(false);
        if (customSettings.get("Packet STOP_SPRINTING")) mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        if (customSettings.get("Packet START_SPRINTING")) mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        if (customSettings.get("CancelServerSprint")) mc.thePlayer.setServerSprintState(false);
    }
}
