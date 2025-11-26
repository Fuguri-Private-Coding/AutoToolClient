package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.SprintEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT, description = "Автоматический ресет спринта после удара.")
public class MoreKB extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("WTap", "LegitFast", "One")
            .setMode("LegitFast");

    DoubleSlider delayTicks = new DoubleSlider("DelayTicks", this, 0,20,2,1);
    DoubleSlider resetTicks = new DoubleSlider("ResetTicks", this, 1,20,2,1);

    final MultiMode dontResetWhile = new MultiMode("Don't Reset While", this)
            .addModes("Target Eating", "Has KnockBack Enchantment", "Target is Burning", "Target is Leaving")
            ;

    final IntegerSetting maxDiff = new IntegerSetting("Max Diff", this, () -> dontResetWhile.get("Target is Leaving"), 0, 180, 90);

    int delay, reset;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();
            if (target != null && target.hurtTime == 10 && !dontReset(target)) {
                delay = delayTicks.getRandomizedIntValue();
                reset = resetTicks.getRandomizedIntValue();
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

            case "One" -> {
                if (event instanceof SprintEvent && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
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
