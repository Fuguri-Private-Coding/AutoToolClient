package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "FullBright", category = Category.VISUAL, description = "Позволяет видеть в темноте.")
public class FullBright extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("NightVision", "Gamma")
            .setMode("NightVision");

    @Override
    public void onDisable() {
        if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
            mc.thePlayer.removePotionEffect(Potion.nightVision.id);
        }

        if (mc.gameSettings.gammaSetting == 10000) mc.gameSettings.gammaSetting = 1;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            switch (mode.getMode()) {
                case "NightVision" -> {
                    if (!mc.thePlayer.isPotionActive(Potion.nightVision)) {
                        mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 255, false, false));
                    }
                }

                case "Gamma" -> {
                    if (mc.gameSettings.gammaSetting != 10000) {
                        mc.gameSettings.gammaSetting = 10000;
                    }
                }
            }
        }
    }
}