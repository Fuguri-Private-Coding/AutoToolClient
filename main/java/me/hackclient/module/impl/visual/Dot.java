package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.combat.KillAura;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(
        name = "Dot",
        category = Category.VISUAL
)
public class Dot extends Module {

    final BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
            KillAura killAura = Client.INSTANCE.getModuleManager().getModule("KillAura");
            if (onlyKillAura.isToggled() && (killAura == null || killAura.getTarget() == null)) {
                return;
            }

            RenderUtils.start3D();
            Vec3 vec = mc.objectMouseOver.hitVec.addVector(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            GL11.glBegin(GL11.GL_POINTS);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glVertex3d(vec.xCoord, vec.yCoord, vec.zCoord);
            GL11.glEnd();
            RenderUtils.stop3D();
        }
    }
}
