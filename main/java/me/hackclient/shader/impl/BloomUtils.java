package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.module.impl.visual.BloomModule;
import me.hackclient.shader.GaussianKernel;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;
import java.util.List;

public class BloomUtils implements InstanceAccess {

	private static Shader bloom;
	private static Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
	private static Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
	private static GaussianKernel gaussianKernel = new GaussianKernel(0);

	public static void drawBloom(List<Runnable> runnables) {
		if (!Display.isVisible() || runnables.isEmpty()) {
			return;
		}

		if (bloom == null) {
			bloom = Client.INSTANCE.getShaderManager().getTestBloom();
		}

		if (mc.displayWidth != inputFramebuffer.framebufferWidth || mc.displayHeight != inputFramebuffer.framebufferHeight) {
			inputFramebuffer.deleteFramebuffer();
			inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

			outputFramebuffer.deleteFramebuffer();
			outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
		} else {
			inputFramebuffer.framebufferClear();
			outputFramebuffer.framebufferClear();
		}

		final int radius = Client.INSTANCE.getModuleManager().getModule(BloomModule.class).radius.getValue();
		final float compression = Client.INSTANCE.getModuleManager().getModule(BloomModule.class).compression.getValue();
		final int programID = bloom.getProgram();

		inputFramebuffer.bindFramebuffer(true);
		runnables.forEach(Runnable::run);

		outputFramebuffer.bindFramebuffer(true);
		bloom.start();

		if (gaussianKernel.getSize() != radius) {
			gaussianKernel = new GaussianKernel(radius);
			gaussianKernel.compute();

			final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
			buffer.put(gaussianKernel.getKernel());
			buffer.flip();

			Uniform.uniform1f(programID, "u_radius", radius);
			Uniform.uniformFB(programID, "u_kernel", buffer);
			Uniform.uniform1i(programID, "u_diffuse_sampler", 0);
			Uniform.uniform1i(programID, "u_other_sampler", 20);
		}

		ScaledResolution sc = new ScaledResolution(mc);

		Uniform.uniform2f(programID, "u_texel_size", 1F / mc.displayWidth, 1F / mc.displayHeight);
		Uniform.uniform2f(programID, "u_direction", compression, 0.0F);
		Uniform.uniform1f(programID, "strength",  Client.INSTANCE.getModuleManager().getModule(BloomModule.class).strength.getValue());

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		inputFramebuffer.bindFramebufferTexture();
		bloom.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());

		mc.getFramebuffer().bindFramebuffer(true);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Uniform.uniform2f(programID, "u_direction", 0.0F, compression);
		outputFramebuffer.bindFramebufferTexture();
		GL13.glActiveTexture(GL13.GL_TEXTURE20);
		inputFramebuffer.bindFramebufferTexture();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		bloom.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
		GlStateManager.disableBlend();
		bloom.stop();
	}
}
