package me.hackclient.shader;

import lombok.Getter;
import me.hackclient.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

@Getter
public class Shader implements Imports {
	final int programId;

	public Shader(ResourceLocation fragmentShaderDir, ResourceLocation vertexShaderDir) {
		programId = GL20.glCreateProgram();
		String vertex = convertFileToString(vertexShaderDir);
		String fragment = convertFileToString(fragmentShaderDir);

		GL20.glAttachShader(programId, createShader(fragment, GL20.GL_FRAGMENT_SHADER));
		GL20.glAttachShader(programId, createShader(vertex, GL20.GL_VERTEX_SHADER));
		GL20.glLinkProgram(programId);
	}

	public static void drawQuad(final double x, final double y, final double width, final double height) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0.0F, 0.0F);
		GL11.glVertex2d(x, y + height);
		GL11.glTexCoord2f(1.0F, 0.0F);
		GL11.glVertex2d(x + width, y + height);
		GL11.glTexCoord2f(1.0F, 1.0F);
		GL11.glVertex2d(x + width, y);
		GL11.glTexCoord2f(0.0F, 1.0F);
		GL11.glVertex2d(x, y);
		GL11.glEnd();
	}

	public static void drawQuad() {
		final ScaledResolution scaledResolution = new ScaledResolution(mc);
		drawQuad(0.0, 0.0, scaledResolution.getScaledWidthD(), scaledResolution.getScaledHeightD());
	}

	private String convertFileToString(ResourceLocation file) {
		final IResourceManager resourceManager = mc.getResourceManager();

		try {
			InputStream inputStream = resourceManager.getResource(file).getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String src = "";

			for (String s; (s = bufferedReader.readLine()) != null; src += s + System.lineSeparator());
			return src;
		} catch (Exception ignored) {}
		return null;
	}

	private int createShader(String oskar, int shaderType) {
		final int shader;
		shader = GL20.glCreateShader(shaderType);
		GL20.glShaderSource(shader, oskar);
		GL20.glCompileShader(shader);
		System.out.println(GL20.glGetShaderInfoLog(shader, 1024));
		return shader;
	}

	public void renderShader(double x,double y , double width, double height) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2d(x, y);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2d(x, y+height);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2d(x+width, y+height);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2d(x+width, y);
		GL11.glEnd();
	}

	public void start() {
		GL20.glUseProgram(programId);
	}

	public static void stop() {
		GL20.glUseProgram(0);
	}

	public void uniformFB(final String name, final FloatBuffer floatBuffer) {
		GL20.glUniform1(getLocation(name), floatBuffer);
	}

	public void uniform1i(final String name, final int i) {
		GL20.glUniform1i(getLocation(name), i);
	}

	public void uniform2i(final String name, final int i, final int j) {
		GL20.glUniform2i(getLocation(name), i, j);
	}

	public void uniform1f(final String name, final float f) {
		GL20.glUniform1f(getLocation(name), f);
	}

	public void uniform2f(final String name, final float f, final float g) {
		GL20.glUniform2f(getLocation(name), f, g);
	}

	public void uniform3f(final String name, final float f, final float g, final float h) {
		GL20.glUniform3f(getLocation(name), f, g, h);
	}

	public void uniform4f(final String name, final float f, final float g, final float h, final float i) {
		GL20.glUniform4f(getLocation(name), f, g, h, i);
	}

	public int getLocation(final String name) {
		return GL20.glGetUniformLocation(programId, name);
	}
}
