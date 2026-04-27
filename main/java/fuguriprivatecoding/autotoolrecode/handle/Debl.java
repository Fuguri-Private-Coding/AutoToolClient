package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.ServerJoinEvent;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.DynamicIsland;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWID;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

public class Debl implements EventListener {

    public Debl() {
        Events.register(this);
    }

    @Override
    public boolean listen() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ServerJoinEvent && Modules.getModule(IRC.class).isToggled()) ClientIRC.connectServer();
        if (event instanceof KeyEvent e) {
            Modules.getModules().forEach(module -> {
                if (module.getKey() == e.getKey()) module.toggle();
            });
        }

        if (event instanceof RenderScreenEvent && !Modules.getModule(DynamicIsland.class).isToggled()) {
            ClientFont fontRenderer = Fonts.fonts.get("SFPro");
            ScaledResolution sc = new ScaledResolution(mc);

            EasingAnimation anim = HWID.noConnectionAnim;
            anim.update(1f, Easing.OUT_BACK);

            long time = System.currentTimeMillis() - HWID.connectionTimer.getLastMS();
            int sec = Integer.parseInt(String.valueOf(time / 1000L));

            int remainingSec = 30 - sec;

            String text = ClientUtils.prefixLog + "Нет интернет подключения, клиент закроется через §9" + remainingSec + "§f s.";

            float x = sc.getScaledWidth() / 2f - fontRenderer.getStringWidth(text) / 2f - 5;
            float y = 5;

            float width = fontRenderer.getStringWidth(text);
            float height = 15;

            ScaleUtils.startScaling(x, y, width, height, anim.getValue());

            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - width / 2f - 5, 5f, width + 5, 15f, 7.5f, Colors.BLACK.withAlphaClamp(0.7f * anim.getValue()));
            fontRenderer.drawCenteredString(text, sc.getScaledWidth() / 2f, 10, Colors.WHITE.withAlphaClamp(anim.getValue()));
            ScaleUtils.stopScaling();
        }
    }

}


