package me.hackclient.module.impl.player;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(
        name = "KillEffects",
        category = Category.PLAYER
)
public class KillEffects extends Module {
    BooleanSetting effect = new BooleanSetting("Effect", this, true);
    MultiBooleanSetting effects = new MultiBooleanSetting("Effects",this, effect::isToggled)
            .add("Lightning Bolt");

    BooleanSetting sound = new BooleanSetting("Sound", this, true);
    MultiBooleanSetting sounds = new MultiBooleanSetting("Sounds",this, sound::isToggled)
            .add("Half-Life-Death");

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            for (Entity ent : mc.theWorld.loadedEntityList) {
                EntityLivingBase entity = Client.INSTANCE.getCombatManager().getTarget();
                if (ent.equals(entity) && ent.isDead) {
                    if (sounds.get("Half-Life-Death")) Client.INSTANCE.getSoundsManager().getKilledSound().asyncPlay(1.0f);
                }
            }
        }
    }
}
