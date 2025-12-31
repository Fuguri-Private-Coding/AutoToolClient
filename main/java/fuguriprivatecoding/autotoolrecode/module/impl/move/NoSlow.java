package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.SlowDownEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "NoSlow", category = Category.MOVE, description = "Позволяет вам не замедлятся при использовании предмета.")
public class NoSlow extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Intave", "Vanilla", "Grim")
            .setMode("Intave")
            ;

    BooleanSupplier vanillaGrim = () -> mode.is("Vanilla") || mode.is("Grim");
    BooleanSupplier vanilla = () -> mode.is("Vanilla");
    BooleanSupplier grim = () -> mode.is("Grim");

    MultiMode itemToSlow = new MultiMode("Items To Slow", this, () -> vanillaGrim.getAsBoolean() || mode.is("Intave"))
        .addModes("Sword", "Consumable", "Bow");

    FloatSetting strafeSwordSlow = new FloatSetting("Strafe Sword Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);
    FloatSetting forwardSwordSlow = new FloatSetting("Forward Sword Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);

    FloatSetting strafeConsumableSlow = new FloatSetting("Strafe Consumable Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);
    FloatSetting forwardConsumableSlow = new FloatSetting("Forward Consumable Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);

    FloatSetting strafeBowSlow = new FloatSetting("Strafe Bow Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);
    FloatSetting forwardBowSlow = new FloatSetting("Forward Bow Slow", this, vanillaGrim, 0f, 1f, 0.2f, 0.01f);

    IntegerSetting grimTicks = new IntegerSetting("Grim Ticks", this, grim, 0, 5, 3);

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0) return;
        if (mc.thePlayer.inventory.getCurrentItem() == null) return;

        boolean work = (isUsingSword() && itemToSlow.get("Sword"))
            || (isUsingConsumable() && itemToSlow.get("Consumable"))
            || (isUsingBow() && itemToSlow.get("Bow"));

        switch (mode.getMode()) {
            case "Intave" -> {
                if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE && work) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                }
            }

            case "Vanilla" -> {
                if (event instanceof SlowDownEvent e) {
                    if (isUsingSword() && itemToSlow.get("Sword")) {
                        e.setForward(forwardSwordSlow.getValue());
                        e.setStrafe(strafeSwordSlow.getValue());
                    }

                    if (isUsingConsumable() && itemToSlow.get("Consumable")) {
                        e.setForward(forwardConsumableSlow.getValue());
                        e.setStrafe(strafeConsumableSlow.getValue());
                    }

                    if (isUsingBow() && itemToSlow.get("Bow")) {
                        e.setForward(forwardBowSlow.getValue());
                        e.setStrafe(strafeBowSlow.getValue());
                    }
                }
            }

            case "Grim" -> {
                if (event instanceof SlowDownEvent e && mc.thePlayer.ticksExisted % grimTicks.getValue() == 0) {
                    if (isUsingSword() && itemToSlow.get("Sword")) {
                        e.setForward(forwardSwordSlow.getValue());
                        e.setStrafe(strafeSwordSlow.getValue());
                    }

                    if (isUsingConsumable() && itemToSlow.get("Consumable")) {
                        e.setForward(forwardConsumableSlow.getValue());
                        e.setStrafe(strafeConsumableSlow.getValue());
                    }

                    if (isUsingBow() && itemToSlow.get("Bow")) {
                        e.setForward(forwardBowSlow.getValue());
                        e.setStrafe(strafeBowSlow.getValue());
                    }
                }
            }
        }
    }

    public boolean isHoldingConsumable() {
        return mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata()) || mc.thePlayer.getHeldItem().getItem() instanceof ItemBucketMilk;
    }

    public boolean isUsingConsumable() {
        return mc.thePlayer.isUsingItem() && isHoldingConsumable();
    }

    public boolean isUsingSword() {
        return mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public boolean isUsingBow() {
        return mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }
}
