package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "MurderMystery", category = Category.MISC)
public class MurderMystery extends Module {

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
                if (murders.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.iron_sword) {
                    murders.add(playerEntity.getName());
                    if (debug.isToggled()) ClientUtils.chatLog("Murder is " + playerEntity.getName());
                }
                if (detectives.contains(playerEntity.getName()) && playerEntity.getHeldItem() != null && playerEntity.getHeldItem().getItem() == Items.bow) {
                    detectives.add(playerEntity.getName());
                    if (debug.isToggled()) ClientUtils.chatLog("Detective is " + playerEntity.getName());
                }
            }
        }
    }
}
