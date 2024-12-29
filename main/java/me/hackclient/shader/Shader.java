package me.hackclient.shader;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Shader implements InstanceAccess {
	private final int program;

	public Shader(ResourceLocation fragmentShaderDir, ResourceLocation vertexShaderDir) {
		program = GL20.glCreateProgram();
		String vertex = convertFileToString(vertexShaderDir);
		String fragment = convertFileToString(fragmentShaderDir);

		GL20.glAttachShader(program, createShader(fragment, GL20.GL_FRAGMENT_SHADER));
		GL20.glAttachShader(program, createShader(vertex, GL20.GL_VERTEX_SHADER));
		GL20.glLinkProgram(program);
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

	public int getProgram() {
		return program;
	}

	public void start() {
		GL20.glUseProgram(program);
	}

	public void stop() {
		GL20.glUseProgram(0);
	}
}
