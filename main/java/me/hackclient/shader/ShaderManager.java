package me.hackclient.shader;

import net.minecraft.util.ResourceLocation;

public class ShaderManager {

	private Shader testBloom, rounded;

	public ShaderManager() {
		init();
	}

	public void init() {
		testBloom = new Shader(getShaderSource("TestBloom.glsl"), getShaderSource("vertex.txt"));
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}

	public Shader getTestBloom() {
		return testBloom;
	}

	public Shader getRounded() {
		return rounded;
	}
}
