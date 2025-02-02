package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "MurderDetector",
        category = Category.MISC
)
public class MurderDetector extends Module {

    List<EntityPlayer> murders;

    public MurderDetector() {
        murders = new ArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                if (murders.contains(playerEntity))
                    continue;

                if (playerEntity.getHeldItem() == null)
                    continue;

                if (playerEntity.getHeldItem().getItem() != Items.iron_sword)
                    continue;

                murders.add(playerEntity);
                ClientUtils.chatLog("Murder is " + playerEntity.getName());
            }
        }
    }
}
