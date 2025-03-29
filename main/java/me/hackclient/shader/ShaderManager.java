package me.hackclient.shader;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader rounded;

	public ShaderManager() {
		init();
	}

	public void init() {
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}