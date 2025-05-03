package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "MurderDetector", category = Category.MISC)
public class MurderDetector extends Module {

    public List<String> murders;

    public MurderDetector() {
        murders = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!murders.isEmpty()) murders.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {
            if (!murders.isEmpty()) murders.clear();
        }
        if (event instanceof TickEvent) {
            for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                if (murders.contains(playerEntity.getName()))
                    continue;

                if (playerEntity.getHeldItem() == null)
                    continue;

                if (playerEntity.getHeldItem().getItem() != Items.iron_sword)
                    continue;

                murders.add(playerEntity.getName());
                ClientUtils.chatLog("Murder is " + playerEntity.getName());
            }
        }
    }
}
