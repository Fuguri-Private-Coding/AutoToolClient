package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

@ModuleInfo(
        name = "TestESP2",
        category = Category.VISUAL
)
public class TESTESP2 extends Module {

    Framebuffer framebuffer;

    public TESTESP2() {
        framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
            framebuffer = createFrameBuffer(framebuffer);
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
                final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(entityPlayer);

                if (mc.getRenderManager() == null || render == null || (entityPlayer == mc.thePlayer)) {
                    continue;
                }

                mc.getRenderManager().renderEntityStatic(entityPlayer, mc.timer.renderPartialTicks, false);
            }
            framebuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);
        }
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            final Shader shader = Client.INSTANCE.getShaderManager().getTestBloom();

            if (shader == null) {
                return;
            }

            final int id = shader.getProgramId();

            shader.start();
            Uniform.uniform1f(id, "radius", 15f);
            Uniform.uniform1i(id, "entityTexture", 19);
            Uniform.uniform1f(id, "u_texel_size", 1f / mc.displayWidth);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
            GL13.glActiveTexture(GL13.GL_TEXTURE19);
            framebuffer.bindFramebufferTexture();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            mc.getFramebuffer().bindFramebuffer(true);
            shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
            GlStateManager.disableBlend();
            shader.stop();
        }
    }

    Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return framebuffer;
    }
}
