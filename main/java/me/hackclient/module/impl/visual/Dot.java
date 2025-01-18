package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
//import me.hackclient.module.impl.combat.KillAura;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation3D;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(
        name = "Dot",
        category = Category.VISUAL,
        toggled = true
)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 1.0f, 20.0f, 10.0f, 0.1f);
    final BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);

    final Animation3D animation3D;

    public Dot() {
        animation3D = new Animation3D();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
//            KillAura killAura = Client.INSTANCE.getModuleManager().getModule("KillAura");
//            if (onlyKillAura.isToggled() && (killAura == null || killAura.getTarget() == null)) return;
            RenderUtils.start3D();
            Vec3 vec = mc.objectMouseOver.hitVec.addVector(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            GL11.glPointSize(size.getValue());
            GL11.glEnable(GL11.GL_POINT_SMOOTH);
            GL11.glBegin(GL11.GL_POINTS);
            animation3D.update(30);
            animation3D.endX = vec.xCoord;
            animation3D.endY = vec.yCoord;
            animation3D.endZ = vec.zCoord;
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glVertex3d(animation3D.x,animation3D.y,animation3D.z);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_POINT_SMOOTH);
            GL11.glPointSize(1);
            RenderUtils.stop3D();
        }
    }
}
