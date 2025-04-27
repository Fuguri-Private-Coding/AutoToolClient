package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.animation.Animation3D;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "Dot", category = Category.VISUAL, toggled = true)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0f, 1f, 0.5f, 0.05f) {};
    final BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);
    final FloatSetting smooth = new FloatSetting("Smooth", this, 0f, 20.0f, 5.0f, 0.1f) {};
    final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);

    Shadows shadows;
    Animation3D pos = new Animation3D();

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render3DEvent) {
            if (Client.INSTANCE.getCombatManager().getTarget() == null && onlyKillAura.isToggled()) { return; }
            MovingObjectPosition mouse = mc.objectMouseOver;

            pos.endX = mouse.hitVec.xCoord;
            pos.endY = mouse.hitVec.yCoord;
            pos.endZ = mouse.hitVec.zCoord;
            pos.update(smooth.getValue());

            if (shadows.isToggled() && shadows.dot.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawDot(pos.x, pos.y, pos.z, size.getValue(), -1));
            RenderUtils.drawDot(pos.x, pos.y, pos.z, size.getValue(), color.getColor().getRGB());
        }
    }
}
