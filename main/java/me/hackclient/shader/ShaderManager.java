package me.hackclient.shader;

import lombok.Getter;
import me.hackclient.shader.impl.RoundedUtils;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader rounded;

	public ShaderManager() {
		init();
	}

	public void init() {
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
		new RoundedUtils();
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}