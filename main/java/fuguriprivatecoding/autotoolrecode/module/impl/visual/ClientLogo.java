package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "ClientLogo", category = Category.VISUAL, description = "Пожалуйста, включите меня.")
public class ClientLogo extends Module {

    ResourceLocation logo = Client.of("image/ratka.png");

    @Override
    public void onEvent(Event event) {
        if (event instanceof RenderScreenEvent) {
            RenderUtils.drawImage(logo, 5, 5, 100, 100);
        }
    }
}
