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
            drawEntityEvent.setCanceled(true);
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
//        if (event instanceof Render3DEvent) {
//            framebuffer = createFrameBuffer(framebuffer);
//            framebuffer.framebufferClear();
//            framebuffer.bindFramebuffer(true);
//            for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
//                final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(entityPlayer);
//
//                if (mc.getRenderManager() == null || render == null || (entityPlayer == mc.thePlayer)) {
//                    continue;
//                }
//
//                mc.getRenderManager().renderEntityStatic(entityPlayer, mc.timer.renderPartialTicks, false);
//            }
//            // тут рендерить то что должно изменяться на шейдер
//            framebuffer.unbindFramebuffer();
//            mc.getFramebuffer().bindFramebuffer(true);
//        }
//        if (event instanceof Render2DEvent) {
//            ScaledResolution sc = new ScaledResolution(mc);
//            final Shader shader = Client.INSTANCE.getShaderManager().getPixelReplacer();
//
//            if (shader == null) {
//                return;
//            }
//
//            final int id = shader.getProgram();
//
//            shader.start();
//            Uniform.uniform1f(id, "r_offset", rOffset.getValue());
//            Uniform.uniform1f(id, "g_offset", gOffset.getValue());
//            Uniform.uniform1f(id, "b_offset", bOffset.getValue());
//            Uniform.uniform1f(id, "time", timer.reachedMS() / 1000f);
//            Uniform.uniform1i(id, "texture", 19);
//            Uniform.uniform1f(id, "texel_size", 1f / mc.displayHeight);
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GlStateManager.enableAlpha();
//            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
//            GL13.glActiveTexture(GL13.GL_TEXTURE19);
//            framebuffer.bindFramebufferTexture();
//            GL13.glActiveTexture(GL13.GL_TEXTURE0);
//            mc.getFramebuffer().bindFramebuffer(true);
//            shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
//            GlStateManager.disableBlend();
//            shader.stop();
//        }
    }
//
//    Framebuffer createFrameBuffer(Framebuffer framebuffer) {
//        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
//            if (framebuffer != null) {
//                framebuffer.deleteFramebuffer();
//            }
//            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
//        }
//        return framebuffer;
//    }
}
