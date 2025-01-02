package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ClientLogo", category = Category.VISUAL)
public class ClientLogo extends Module {

    IntegerSetting X = new IntegerSetting("X", this, 0, 10, 0);
    IntegerSetting Y = new IntegerSetting("Y", this, 0, 10, 0);
    IntegerSetting scale = new IntegerSetting("Scale", this, 0, 500, 50);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            X.setMax(sc.getScaledWidth());
            Y.setMax(sc.getScaledHeight());
            ResourceLocation molotok400panage = new ResourceLocation("minecraft", "hackclient/image/molotok400.png");
            Bloom bloomModule = InstanceAccess.mm.getModule(Bloom.class);
            if (bloomModule.isToggled() && bloomModule.clientLogo.isToggled()) {
                List<Runnable> list = new ArrayList<>();
                list.add(() -> RenderUtils.drawImage(molotok400panage, X.getValue(), Y.getValue(), scale.getValue(), scale.getValue()));
                BloomUtils.drawBloom(list);
            }
            RenderUtils.drawImage(molotok400panage, X.getValue(), Y.getValue(), scale.getValue(), scale.getValue());

        }
    }
}
