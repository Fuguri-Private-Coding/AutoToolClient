package fuguriprivatecoding.autotoolrecode.utils.render.shader;

import fuguriprivatecoding.autotoolrecode.Client;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;

@UtilityClass
public class Shaders {

	public Shader rounded, background, gaussianBlur, bloom, alpha, roundedgrad, mb, testelips;

	public void init() {
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));
		roundedgrad = new Shader(getShaderSource("roundedgrad.glsl"), getShaderSource("vertex.txt"));

        bloom = new Shader(getShaderSource("bloom.frag"), getShaderSource("vertex.txt"));

		alpha = new Shader(getShaderSource("alpha.glsl"), getShaderSource("vertex.txt"));
		mb = new Shader(getShaderSource("mb.glsl"), getShaderSource("vertex.txt"));

		testelips = new Shader(getShaderSource("testelips.glsl"), getShaderSource("vertex.txt"));

		gaussianBlur = new Shader(getShaderSource("blur.glsl"), getShaderSource("vertex.txt"));

		background = new Shader(getShaderSource("background.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return Client.INST.of("shaders/" + name);
	}
}