package me.hackclient.shader;

import lombok.Getter;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.shader.impl.TestBloomUtils;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader testBloom, rounded, textFade, pixelReplacer, glowEsp, bloom;

	public ShaderManager() {
		init();
	}

	public void init() {
		bloom = new Shader(getShaderSource("bloom.glsl"), getShaderSource("vertex.txt"));
		glowEsp = new Shader(getShaderSource("GlowEsp.glsl"), getShaderSource("vertex.txt"));
		pixelReplacer = new Shader(getShaderSource("PixelReplacer.glsl"), getShaderSource("vertex.txt"));
		textFade = new Shader(getShaderSource("TextFade.glsl"), getShaderSource("vertex.txt"));
		testBloom = new Shader(getShaderSource("TestBloom.glsl"), getShaderSource("vertex.txt"));
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));

		new PixelReplacerUtils();
		new TestBloomUtils();
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}