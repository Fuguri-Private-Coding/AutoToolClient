package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.SprintEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT, description = "Автоматический ресет спринта после удара.")
public class MoreKB extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("WTap", "LegitFast")
            .setMode("LegitFast");


    final IntegerSetting minDelay = new IntegerSetting("Min Delay After Hit", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };
    final IntegerSetting maxDelay = new IntegerSetting("Max Delay After Hit", this, 0, 10, 3) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return super.getValue();
        }
    };

    final IntegerSetting minReset = new IntegerSetting("Min Reset Duration", this, 1, 5, 1) {
        @Override
        public int getValue() {
            if (maxReset.value < value) { value = maxReset.value; }
            return super.getValue();
        }
    };
    final IntegerSetting maxReset = new IntegerSetting("Max Reset Duration", this, 1, 5, 1) {
        @Override
        public int getValue() {
            if (minReset.value > value) { value = minReset.value; }
            return super.getValue();
        }
    };

    final MultiMode dontResetWhile = new MultiMode("Don't Reset While", this)
            .addModes("Target Eating", "Has KnockBack Enchantment", "Target is Burning", "Target is Leaving")
            ;

    final IntegerSetting maxDiff = new IntegerSetting("Max Diff", this, () -> dontResetWhile.get("Target is Leaving"), 0, 180, 90);

    int delay, reset;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            EntityLivingBase target = Client.INST.getCombatManager().getTargetOrSelectedEntity();
            if (target != null && target.hurtTime == 10 && !dontReset(target)) {
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

            case "LegitFast" -> {
                if (event instanceof SprintEvent && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.setSprinting(false);
                    reset--;
                }
            }
        }
    }

    boolean dontReset(EntityLivingBase target) {
        if (dontResetWhile.get("Target Eating") && target.isEating()) return true;
        if (dontResetWhile.get("Has KnockBack Enchantment") && hasKnockBackEnchantment(mc.thePlayer.inventory.getCurrentItem())) return true;
        if (dontResetWhile.get("Target is Burning") && target.isBurning()) return true;
        float targetMoveYaw = RotUtils.getRotationFromDiff(target.getPositionVector().subtract(target.getPrevPositionVector())).getYaw();
        float delta = MathHelper.wrapDegree(mc.thePlayer.rotationYaw - targetMoveYaw);
        return dontResetWhile.get("Target is Leaving") && delta <= maxDiff.getValue();
    }

    private boolean hasKnockBackEnchantment(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() != null && EnchantmentHelper.getEnchantments(itemStack).containsKey(Enchantment.knockback.effectId);
    }

    @Override
    public String getSuffix() {
        return String.valueOf(mode.getMode());
    }
}
