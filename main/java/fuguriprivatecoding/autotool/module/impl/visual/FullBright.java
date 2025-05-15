package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "FullBright", category = Category.VISUAL)
public class FullBright extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("NightVision", "Gamma")
            .setMode("NightVision");

    @Override
    public void onDisable() {
        if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
            mc.thePlayer.removePotionEffect(Potion.nightVision.id);
        }
        mc.gameSettings.gammaSetting = 1;
    }

    @Override
    public void onEnable() {
        switch (mode.getMode()) {
            case "NightVision" -> mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 255, false, false));
            case "Gamma" -> mc.gameSettings.gammaSetting = 10000;
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mode.getMode().equals("NightVision") && !mc.thePlayer.isPotionActive(Potion.nightVision)) {
                mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 255, false, false));
            }
        }
    }
}