package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.Mode;
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
