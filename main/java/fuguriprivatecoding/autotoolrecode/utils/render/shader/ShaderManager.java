package fuguriprivatecoding.autotoolrecode.utils.render.shader;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Getter
public class ShaderManager {

	Shader rounded, background, bloom;

	public ShaderManager() {
		init();
	}

	public void init() {
		rounded = new Shader(getShaderSource("rounded.glsl"), getShaderSource("vertex.txt"));

		bloom = new Shader(getShaderSource("bloom.glsl"), getShaderSource("vertex.txt"));


		background = new Shader(getShaderSource("background.glsl"), getShaderSource("vertex.txt"));
	}

	private ResourceLocation getShaderSource(String name) {
		return new ResourceLocation("minecraft","hackclient/shaders/" + name);
	}
}