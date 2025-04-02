package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "FullBright", category = Category.VISUAL, toggled = true)
public class FullBright extends Module {

    ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "NightVision",
            new String[]{
                    "NightVision",
                    "Gamma"
            }
    );

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

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            if (mode.getMode().equals("NightVision") && !mc.thePlayer.isPotionActive(Potion.nightVision)) {
                mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, Integer.MAX_VALUE, 255, false, false));
            }
        }
    }
}