package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import java.awt.*;

@ModuleInfo(name = "Test", category = Category.MISC, description = "тестовый модуль.")
public class Test extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ClientFontRenderer font = Client.INST.getFonts().fonts.get("SFPro");

            if (font != null) {
                font.drawString("Дебил Тупой | §lDebil Tupoi", e.getSc().getScaledWidth() / 2f - font.getStringWidth("Дебил Тупой | Debil Tupoi") / 2f, 22, Color.WHITE, false);
                font.drawString("Дебил Тупой | §oDebil Tupoi", e.getSc().getScaledWidth() / 2f - font.getStringWidth("Дебил Тупой | Debil Tupoi") / 2f, 48, Color.WHITE, false);
                font.drawString("Дебил Тупой | §dDebil Tupoi", e.getSc().getScaledWidth() / 2f - font.getStringWidth("Дебил Тупой | Debil Tupoi") / 2f, 40 + 32, Color.WHITE, false);
                font.drawString("Дебил Тупой | §mDebil Tupoi", e.getSc().getScaledWidth() / 2f - font.getStringWidth("Дебил Тупой | Debil Tupoi") / 2f, 40 + 20 + 40, Color.WHITE, false);
            }

        }
    }
}