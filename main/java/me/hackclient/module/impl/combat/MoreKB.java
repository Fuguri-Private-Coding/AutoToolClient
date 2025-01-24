package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import static net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SPRINTING;

@ModuleInfo(
        name = "MoreKB",
        category = Category.COMBAT
)
public class MoreKB extends Module {

    final StopWatch stopWatch;

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "LegitFast",
            new String[] {
                    "Legit",
                    "LegitFast",
                    "LegitSneak",
                    "LegitBlock"
            }
    );

    final IntegerSetting minDelay = new IntegerSetting("MinTickDelayAfterHit", this, 0, 10, 3);
    final IntegerSetting maxDelay = new IntegerSetting("MinTickDelayAfterHit", this, 0, 10, 3);
    final IntegerSetting minReset = new IntegerSetting("MinTickResetDuration", this, 0, 10, 1);
    final IntegerSetting maxReset = new IntegerSetting("MaxTickResetDuration", this, 0, 10, 1);
    final IntegerSetting delayBetweenHit = new IntegerSetting("DelayBetweenHit", this, 0, 500, 450);

    public MoreKB() {
        stopWatch = new StopWatch();
    }

    int delay, reset;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof AttackEvent && stopWatch.reachedMS(delayBetweenHit.getValue())) {
            delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
            reset = RandomUtils.nextInt(minReset.getValue(), maxReset.getValue());
            stopWatch.reset();
        }

        if (delay > 0) {
            if (event instanceof TickEvent) delay--;
            return;
        }

        if (reset == 0) return;

        switch (mode.getMode()) {
            case "Legit" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setForward(false);
                }
            }

            case "LegitFast" -> {
                if (event instanceof SprintEvent) {
                    mc.thePlayer.setSprinting(false);
                    mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, STOP_SPRINTING));
                    mc.thePlayer.setServerSprintState(false);
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
}
