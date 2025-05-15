package fuguriprivatecoding.autotool.module.impl.combat;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotool.event.events.SprintEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import fuguriprivatecoding.autotool.event.events.*;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.*;
import fuguriprivatecoding.autotool.utils.math.RandomUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT)
public class MoreKB extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("One", "WTap", "STap", "LegitFast")
            .setMode("LegitFast");

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

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            EntityLivingBase target = Client.INST.getCombatManager().getTargetOrSelectedEntity();
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

            case "LegitFast" -> {
                if (event instanceof SprintEvent && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.setSprinting(false);
                    reset--;
                }
            }

            case "One" -> {
                if (event instanceof SprintEvent && !mc.thePlayer.isServerSprintState()) {
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
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
