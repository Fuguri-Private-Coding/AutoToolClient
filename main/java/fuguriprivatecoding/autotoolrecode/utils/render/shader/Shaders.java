package fuguriprivatecoding.autotoolrecode.utils.render.shader;

import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;

@UtilityClass
public class Shaders {

	public Shader rounded, background, bloom, gaussianBlur, bloomReal, alpha;

	public void init() {
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));

		bloom = new Shader(getShaderSource("bloom.glsl"), getShaderSource("vertex.txt"));
		bloomReal = new Shader(getShaderSource("bloomreal.glsl"), getShaderSource("vertex.txt"));

		alpha = new Shader(getShaderSource("alpha.glsl"), getShaderSource("vertex.txt"));

		gaussianBlur = new Shader(getShaderSource("blur.glsl"), getShaderSource("vertex.txt"));

		background = new Shader(getShaderSource("background.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","autotool/shaders/" + name);
	}
}