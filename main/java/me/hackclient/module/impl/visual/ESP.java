package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

@ModuleInfo(
        name = "ESP",
        category = Category.VISUAL
)
public class ESP extends Module {

    final MultiBooleanSetting modes = new MultiBooleanSetting("Modes", this)
            .add("Box")
            //.add("")
            ;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

            for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                RenderUtils.renderHitBox(playerEntity.getEntityBoundingBox());
            }

            GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            RenderUtils.stop3D();
        }
    }
}
