package me.hackclient.module.impl.player;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(name = "KillEffects", category = Category.PLAYER)
public class KillEffects extends Module {

    BooleanSetting effect = new BooleanSetting("Effect", this, true);
    MultiBooleanSetting effects = new MultiBooleanSetting("Effects",this, effect::isToggled)
            .add("Lightning Bolt");

    BooleanSetting sound = new BooleanSetting("Sound", this, true);
    ModeSetting sounds = new ModeSetting("Sounds",
            this,
            sound::isToggled,
            "None",
            new String[] {
                    "Half-Life-Death",
                    "Skeet",
                    "NeverLose",
    });

    FloatSetting volume = new FloatSetting("Volume", this, sound::isToggled, 0,1,1,0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            for (Entity ent : mc.theWorld.loadedEntityList) {
                EntityLivingBase entity = Client.INSTANCE.getCombatManager().getTargetOrSelectedEntity();
                if (ent.equals(entity) && ent.isDead) {
                    if (sound.isToggled()) {
                        switch (sounds.getMode()) {
                            case "Half-Life-Death" -> Client.INSTANCE.getSoundsManager().getKilledSound().asyncPlay(volume.getValue());
                            case "Skeet" -> Client.INSTANCE.getSoundsManager().getSkeetSound().asyncPlay(volume.getValue());
                            case "NeverLose" -> Client.INSTANCE.getSoundsManager().getNeverLoseSound().asyncPlay(volume.getValue());
                        }
                    }
                }
            }
        }
    }
}
