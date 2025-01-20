package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.DrawEntityEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.shader.impl.PixelReplacerUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.player.EntityPlayer;


@ModuleInfo(
        name = "TestESP",
        category = Category.VISUAL
)
public class TestESP extends Module {

    public static boolean kostil;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof DrawEntityEvent drawEntityEvent) {
            //drawEntityEvent.setCanceled(true);
        }
        if (event instanceof Render3DEvent) {
            kostil = true;
            PixelReplacerUtils.addToDraw(() -> {
                for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
                    final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(entityPlayer);

                    if (mc.getRenderManager() == null || render == null || (entityPlayer == mc.thePlayer)) {
                        continue;
                    }

                    mc.getRenderManager().renderEntityStatic(entityPlayer, mc.timer.renderPartialTicks, false);
                }
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
            });
            kostil = false;
        }
    }
}
