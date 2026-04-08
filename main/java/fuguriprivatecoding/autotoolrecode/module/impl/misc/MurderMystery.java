package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.PreAttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "MurderMystery", category = Category.MISC, description = "Позволяет видеть кто Убийца и Детектив.")
public class MurderMystery extends Module {

    CheckBox checkMurders = new CheckBox("CheckMurders", this);
    CheckBox checkDetectives = new CheckBox("CheckDetectives", this);

    CheckBox silentMurder = new CheckBox("SilentMurder", this);
    IntegerSetting switchBackDelay = new IntegerSetting("SwitchBackDelay", this, silentMurder::isToggled, 0, 10, 1);

    CheckBox debug = new CheckBox("Debug", this);

    public static List<String> murders = new ArrayList<>(), detectives = new ArrayList<>();

    private boolean swapped = false;

    StopWatch watch = new StopWatch();

    @Override
    public void onDisable() {
        super.onDisable();
        if (!murders.isEmpty()) murders.clear();
        if (!detectives.isEmpty()) detectives.clear();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {
            if (!murders.isEmpty()) murders.clear();
            if (!detectives.isEmpty()) detectives.clear();
        }

        if (silentMurder.isToggled()) {
            if (event instanceof PreAttackEvent e && e.getHittingEntity() instanceof EntityPlayer player && !player.isFriend()) {
                int slot = getSwordSlot();

                if (slot != -1) {
                    mc.thePlayer.inventory.currentItem = slot;
                    watch.reset();
                    swapped = true;
                }
            }

            if (event instanceof LegitClickTimingEvent && swapped && watch.reachedMS(switchBackDelay.getValue() * 50L)) {
                mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
                swapped = false;
            }
        }

        if (event instanceof TickEvent) {
            for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                if (checkMurders.isToggled() && !murders.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.iron_sword) {
                    murders.add(playerEntity.getName());
                    if (debug.isToggled()) ClientUtils.chatLog("Murder is " + playerEntity.getName());
                }
                if (checkDetectives.isToggled()) {
                    if (!detectives.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.bow) {
                        detectives.add(playerEntity.getName());
                        if (debug.isToggled()) ClientUtils.chatLog("Detective is " + playerEntity.getName());
                    }
                    if (detectives.contains(playerEntity.getName()) && playerEntity.isDead) detectives.remove(playerEntity.getName());
                }
            }
        }
    }

    private int getSwordSlot() {
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null || !(item.getItem() instanceof ItemSword)) continue;

            bestSlot = i;
        }

        return bestSlot;
    }

    public static boolean isMurder(String name) {
        return Modules.getModule(MurderMystery.class).isToggled() && !murders.isEmpty() && murders.contains(name);
    }

    public static boolean isMurder(EntityPlayer entity) {
        return isMurder(entity.getName());
    }

    public static boolean isDetective(String name) {
        return Modules.getModule(MurderMystery.class).isToggled() && !detectives.isEmpty() && detectives.contains(name);
    }

    public static boolean isDetective(EntityPlayer entity) {
        return isDetective(entity.getName());
    }
}
