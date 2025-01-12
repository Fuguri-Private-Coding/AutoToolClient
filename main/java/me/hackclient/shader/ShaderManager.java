package me.hackclient.shader;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader testBloom, rounded, textFade;

	public ShaderManager() {
		init();
	}

	public void init() {
		textFade = new Shader(getShaderSource("TextFade.glsl"), getShaderSource("vertex.txt"));
		testBloom = new Shader(getShaderSource("TestBloom.glsl"), getShaderSource("vertex.txt"));
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}
