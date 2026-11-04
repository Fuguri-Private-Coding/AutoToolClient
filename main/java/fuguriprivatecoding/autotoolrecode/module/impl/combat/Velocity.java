package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.handle.Player;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.Random;

@ModuleInfo(name = "Velocity", category = Category.COMBAT, description = "Позволяет откидыватся меньше.")
public class Velocity extends Module {

    final MultiMode mode = new MultiMode("Mode", this)
            .addModes("Cancel", "Jump", "Reduce");

    final IntegerSetting horizontal = new IntegerSetting("Horizontal", this,() -> mode.get("Cancel"), -100, 100, 0);
    final IntegerSetting vertical = new IntegerSetting("Vertical", this,() -> mode.get("Cancel"), 0, 100, 100);
    final CheckBox onlyOnLadder = new CheckBox("OnlyOnLadder", this, () -> mode.get("Cancel"), false);

    private final DoubleSlider hurtTime = new DoubleSlider("HurtTime", this, () -> mode.get("Reduce"), 1, 10, 1, 10, 1);
    private final IntegerSetting chance = new IntegerSetting("Chance", this, () -> mode.get("Reduce"), 0, 100, 100);
    private final CheckBox onlyAtSprint = new CheckBox("Only at sprint", this, () -> mode.get("Reduce"), true);
    private final FloatSetting multiplier = new FloatSetting("Multiplier", this, () -> mode.get("Reduce"), 0f, 1f, 0.6f, 0.01f);
    private final FloatSetting kbMultiplier = new FloatSetting("Multiplier with knockback item", this, () -> mode.get("Reduce"), 0f, 1f, 0.46f, 0.01f);

    final IntegerSetting jumpChance = new IntegerSetting("Chance", this, () -> mode.get("Jump"), 0,100,80);

    private final Random rand = new Random();

    @Override
    public void onEvent(Event event) {
        if (mode.get("Cancel")) {
            if (event instanceof PacketEvent e) {
                Packet packet = e.getPacket();

                if (packet instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                    if (onlyOnLadder.isToggled()) {
                        if (mc.thePlayer.isOnLadder()) e.cancel();
                    } else {
                        e.cancel();
                    }

                    double needMotionX = s12.getMotionX() / 8000d;
                    double needMotionY = s12.getMotionY() / 8000d;
                    double needMotionZ = s12.getMotionZ() / 8000d;

                    double deltaMotionX = needMotionX - mc.thePlayer.motionX;
                    double deltaMotionY = needMotionY - mc.thePlayer.motionY;
                    double deltaMotionZ = needMotionZ - mc.thePlayer.motionZ;

                    deltaMotionX *= horizontal.getValue() / 100f;
                    deltaMotionY *= vertical.getValue() / 100f;
                    deltaMotionZ *= horizontal.getValue() / 100f;

                    mc.thePlayer.motionX += deltaMotionX;
                    mc.thePlayer.motionY += deltaMotionY;
                    mc.thePlayer.motionZ += deltaMotionZ;
                }
            }
        }

        if (mode.get("Reduce")) {
            if (event instanceof AttackEvent e && e.getHittingEntity() instanceof EntityLivingBase) {
                if (!hurtTime.in(mc.thePlayer.hurtTime))
                    return;

                if (Math.random() > chance.getValue() / 100f)
                    return;

                if (onlyAtSprint.isToggled() && !mc.thePlayer.isSprinting())
                    return;

                double xzMultiplier = hasKnockbackInHand() ? kbMultiplier.getValue() : multiplier.getValue();

                mc.thePlayer.motionX *= xzMultiplier;
                mc.thePlayer.motionZ *= xzMultiplier;
                mc.thePlayer.setSprinting(false);
            }
        }

        if (mode.get("Jump")) {
            if (event instanceof MoveButtonEvent e) {
                if (needJump()) {
                    e.setJump(true);
                }
            }
        }
    }

    public boolean hasKnockbackInHand() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem != null && EnchantmentHelper.getEnchantments(heldItem).containsKey(Enchantment.knockback.effectId);
    }

    private boolean needJump() {
        return !mc.thePlayer.isBurning() && Player.getInstance().fallDistance == 0 && mc.thePlayer.hurtTime == 9 && mc.thePlayer.onGround && mc.thePlayer.isSprinting() && rand.nextInt(100) <= jumpChance.getValue();
    }
}
