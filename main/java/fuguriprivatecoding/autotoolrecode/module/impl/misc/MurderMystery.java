package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "MurderMystery", category = Category.MISC)
public class MurderMystery extends Module {

    CheckBox checkMurders = new CheckBox("CheckMurders", this);
    CheckBox checkDetectives = new CheckBox("CheckDetectives", this);

    CheckBox debug = new CheckBox("Debug", this);

    public List<String> murders, detectives;

    public MurderMystery() {
        murders = new ArrayList<>();
        detectives = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!murders.isEmpty()) murders.clear();
        if (!detectives.isEmpty()) detectives.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {
            if (!murders.isEmpty()) murders.clear();
            if (!detectives.isEmpty()) detectives.clear();
        }
        if (event instanceof TickEvent) {
            for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                if (checkMurders.isToggled() && murders.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.iron_sword) {
                    murders.add(playerEntity.getName());
                    if (debug.isToggled()) ClientUtils.chatLog("Murder is " + playerEntity.getName());
                }
                if (checkDetectives.isToggled() && detectives.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.bow) {
                    detectives.add(playerEntity.getName());
                    if (debug.isToggled()) ClientUtils.chatLog("Detective is " + playerEntity.getName());
                }
            }
        }
    }
}
