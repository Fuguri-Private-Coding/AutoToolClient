package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.SprintEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(name = "MoreKB", category = Category.COMBAT, description = "Автоматический ресет спринта после удара.")
public class MoreKB extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("WTap", "Sprint", "Packet")
            .setMode("Sprint");

    DoubleSlider delayTicks = new DoubleSlider("DelayTicks", this, 0,20,2,1);
    DoubleSlider resetTicks = new DoubleSlider("ResetTicks", this, 1,20,2,1);

    final MultiMode notWhile = new MultiMode("NotWhile", this)
            .addModes("Target Eating", "Has KnockBack Enchantment")
            ;

    int delay, reset;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent e) {
            EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();

            if (target == null) {
                delay = 0;
                reset = 0;
                return;
            }

            if (target.hurtResistantTime == 20 && !notWhile(target) && delay == 0 && reset == 0) {
                delay = delayTicks.getRandomizedIntValue();
                reset = resetTicks.getRandomizedIntValue();
            }

            if (delay > 0 && !TimerRange.teleporting && TimerRange.balance == 0 && !e.isCanceled()) {
                delay--;
            }
        }

        if (reset == 0 || delay > 0) return;

        switch (mode.getMode()) {
            case "WTap" -> {
                if (event instanceof MoveButtonEvent e) {
                    e.setForward(false);
                    reset--;
                }
            }

            case "Sprint" -> {
                if (event instanceof SprintEvent e && mc.thePlayer.isSprinting()) {
                    e.setSprinting(false);
                    ClientUtils.chatLog(reset + "debil");
                    reset--;
                }
            }

            case "Packet" -> {
                if (event instanceof SprintEvent && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    reset--;
                }
            }
        }
    }

    boolean notWhile(EntityLivingBase target) {
        return (notWhile.get("Target Eating") && target.isEating()) ||
            (notWhile.get("Has KnockBack Enchantment") && hasEnchant(mc.thePlayer.inventory.getCurrentItem()));
    }

    private boolean hasEnchant(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() != null && EnchantmentHelper.getEnchantments(itemStack).containsKey(Enchantment.knockback.effectId);
    }
}
