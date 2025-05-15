package me.hackclient.module.impl.player;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(name = "KillEffects", category = Category.PLAYER)
public class KillEffects extends Module {

    CheckBox sound = new CheckBox("Sound", this, true);
    Mode sounds = new Mode("Sounds", this, sound::isToggled)
            .addModes("Half-Life-Death", "Skeet", "NeverLose")
            .setMode("Half-Life-Death");

    FloatSetting volume = new FloatSetting("Volume", this, sound::isToggled, 0,1,1,0.1f);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Entity ent : mc.theWorld.loadedEntityList) {
                EntityLivingBase entity = Client.INST.getCombatManager().getTargetOrSelectedEntity();
                if (ent.equals(entity) && ent.isDead) {
                    if (sound.isToggled()) {
                        switch (sounds.getMode()) {
                            case "Half-Life-Death" -> Client.INST.getSoundsManager().getKilledSound().asyncPlay(volume.getValue());
                            case "Skeet" -> Client.INST.getSoundsManager().getSkeetSound().asyncPlay(volume.getValue());
                            case "NeverLose" -> Client.INST.getSoundsManager().getNeverLoseSound().asyncPlay(volume.getValue());
                        }
                    }
                }
            }
        }
    }
}
