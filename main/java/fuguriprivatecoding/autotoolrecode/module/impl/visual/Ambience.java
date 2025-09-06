package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Ambience", category = Category.VISUAL, description = "Изменяет погоду/время.")
public class Ambience extends Module {

    IntegerSetting time = new IntegerSetting("Time", this, 0, 20, 20);

    public Mode weather = new Mode("Weather", this)
            .addModes("Clear","Snow")
            .setMode("Clear")
            ;

    BooleanSupplier snow = () -> weather.getMode().equals("Snow");

    public ColorSetting color = new ColorSetting("Color", this, snow,1f, 1f, 1f, 1f);

    @Override
    public void onDisable() {
        mc.theWorld.setRainStrength(0);
        mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
        mc.theWorld.getWorldInfo().setRainTime(0);
        mc.theWorld.getWorldInfo().setThunderTime(0);
        mc.theWorld.getWorldInfo().setRaining(false);
        mc.theWorld.getWorldInfo().setThundering(false);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S03PacketTimeUpdate) packetEvent.setCanceled(true);
        if (event instanceof Render3DEvent) mc.theWorld.setWorldTime(time.getValue() * 1000L);
        if (event instanceof TickEvent) {
            if (mc.thePlayer.ticksExisted % 20 == 0) {
                switch (this.weather.getMode()) {
                    case "Clear" -> {
                        mc.theWorld.setRainStrength(0);
                        mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
                        mc.theWorld.getWorldInfo().setRainTime(0);
                        mc.theWorld.getWorldInfo().setThunderTime(0);
                        mc.theWorld.getWorldInfo().setRaining(false);
                        mc.theWorld.getWorldInfo().setThundering(false);
                    }
                    case "Snow" -> {
                        mc.theWorld.setRainStrength(1);
                        mc.theWorld.getWorldInfo().setCleanWeatherTime(0);
                        mc.theWorld.getWorldInfo().setRainTime(Integer.MAX_VALUE);
                        mc.theWorld.getWorldInfo().setThunderTime(Integer.MAX_VALUE);
                        mc.theWorld.getWorldInfo().setRaining(true);
                        mc.theWorld.getWorldInfo().setThundering(false);
                    }
                }
            }
        }
    }

    public float getFloatTemperature(BlockPos blockPos, BiomeGenBase biomeGenBase) {
        if (isToggled()) {
            switch (this.weather.getMode()) {
                case "Snow" -> {
                    return 0.1F;
                }
            }
        }

        return biomeGenBase.getFloatTemperature(blockPos);
    }

    public boolean skipRainParticles() {
        final String name = weather.getMode();
        return isToggled() && name.equals("Snow");
    }
}
