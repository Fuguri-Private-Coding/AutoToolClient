package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.math.RandomUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(
        name = "MoreKB",
        category = Category.COMBAT
)
public class MoreKB extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "LegitFast",
            new String[] {
                    "WTap",
                    "STap",
                    "LegitFast",
                    "SneakTap",
                    "BlockHit",
                    "Custom"
            }
    );

    final IntegerSetting minDelay = new IntegerSetting("MinDelayAfterHit", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };

    final IntegerSetting maxDelay = new IntegerSetting("MaxDelayAfterHit", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return super.getValue();
        }
    };

    final IntegerSetting minReset = new IntegerSetting("MinResetDuration", this, 1, 5, 1) {
        @Override
        public int getValue() {
            if (maxReset.value < value) { value = maxReset.value; }
            return super.getValue();
        }
    };

    final IntegerSetting maxReset = new IntegerSetting("MaxResetDuration", this, 1, 5, 1) {
        @Override
        public int getValue() {
            if (minReset.value > value) { value = minReset.value; }
            return super.getValue();
        }
    };

    int delay, reset;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();
            if (target != null && target.hurtTime == 10) {
                delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
                reset = RandomUtils.nextInt(minReset.getValue(), maxReset.getValue());
            }
        }

        if (delay > 0) {
            if (event instanceof TickEvent) delay--;
            return;
        }

        if (reset == 0) return;

        switch (mode.getMode()) {
            case "WTap" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setForward(false);
                    reset--;
                }
            }

            case "STap" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setForward(false);
                    moveButtonEvent.setBack(true);
                    reset--;
                }
            }

            case "SneakTap" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    moveButtonEvent.setSneak(true);
                    reset--;
                }
            }

            case "LegitFast" -> {
                if (event instanceof SprintEvent && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.setSprinting(false);
                    reset--;
                }
            }

            case "BlockHit" -> {
                if (mc.thePlayer.getHeldItem() == null)
                    break;

                if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))
                    break;

                if (event instanceof RunGameLoopEvent) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    reset--;
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(mode.getMode());
    }
}
