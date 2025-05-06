package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.DrawBlockHighlightEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.player.Scaffold;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL)
public class BlockOverlay extends Module {

    ColorSetting color = new ColorSetting("Color", this, 0, 0.5f, 1f, 0.3f);
    FloatSetting lineAlpha = new FloatSetting("LineAlpha",this, 0, 1, 1, 0.1f);
    FloatSetting lineWidth = new FloatSetting("LineWidth",this, 0, 5, 1, 0.1f);
    Shadows shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isToggled()) {
            return;
        }
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (event instanceof DrawBlockHighlightEvent) {
            MovingObjectPosition renderRayCast = mc.objectMouseOver;
            if (renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                RenderUtils.start3D();
                if (shadows.isToggled() && shadows.module.get("BlockOverlay")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), 1,1,1,1, 0f, lineWidth.getValue()));
                RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), lineAlpha.getValue(), lineWidth.getValue());
                RenderUtils.stop3D();
            }
        }
    }
}
