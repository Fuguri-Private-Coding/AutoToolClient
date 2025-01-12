package me.hackclient.shader;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader testBloom, rounded, textFade, RUSSIANSHADER, glowEsp;

	public ShaderManager() {
		init();
	}

	public void init() {
		glowEsp = new Shader(getShaderSource("GlowEsp.glsl"), getShaderSource("vertex.txt"));
		RUSSIANSHADER = new Shader(getShaderSource("RUSSIANSHADER.glsl"), getShaderSource("vertex.txt"));
		textFade = new Shader(getShaderSource("TextFade.glsl"), getShaderSource("vertex.txt"));
		testBloom = new Shader(getShaderSource("TestBloom.glsl"), getShaderSource("vertex.txt"));
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}
