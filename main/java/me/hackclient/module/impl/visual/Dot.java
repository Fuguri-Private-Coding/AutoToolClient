package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "Dot", category = Category.VISUAL, toggled = true)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0f, 1f, 0.5f, 0.05f) {};
    final BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);
    final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);

    Shadows shadows;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render3DEvent) {
            if (Client.INSTANCE.getCombatManager().getTarget() == null && onlyKillAura.isToggled()) { return; }
            MovingObjectPosition mouse = mc.objectMouseOver;

            if (shadows.isToggled() && shadows.module.get("Dot")) BloomUtils.addToDraw(() -> RenderUtils.drawDot(mouse.hitVec.xCoord,mouse.hitVec.yCoord,mouse.hitVec.zCoord, size.getValue(), -1));
            RenderUtils.drawDot(mouse.hitVec.xCoord,mouse.hitVec.yCoord,mouse.hitVec.zCoord, size.getValue(), color.getColor().getRGB());
        }
    }
}
