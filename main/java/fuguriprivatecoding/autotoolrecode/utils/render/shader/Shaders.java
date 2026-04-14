package fuguriprivatecoding.autotoolrecode.utils.render.shader;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;

@UtilityClass
public class Shaders {

	public Shader roundedRect, background, gaussianBlur, bloom, motionBlur, roundedRectTest, msdfFonts;

	public void init() {
		roundedRect = new Shader(getShaderSource("roundedRect.glsl"), getShaderSource("vertex.txt"));
		roundedRectTest = new Shader(getShaderSource("roundedRectTest.glsl"), getShaderSource("vertex.txt"));
		msdfFonts = new Shader(getShaderSource("msdfFonts.glsl"), getShaderSource("vertex.txt"));
		gaussianBlur = new Shader(getShaderSource("gaussianBlur.glsl"), getShaderSource("vertex.txt"));
        bloom = new Shader(getShaderSource("bloom.glsl"), getShaderSource("vertex.txt"));
		background = new Shader(getShaderSource("background.glsl"), getShaderSource("vertex.txt"));
		motionBlur = new Shader(getShaderSource("motionBlur.glsl"), getShaderSource("vertex.txt"));

		ClientUtils.chatLog("Успешно инициализировал шейдеры.");
	}

	private ResourceLocation getShaderSource(String name) {
		return Client.INST.of("shaders/" + name);
	}
}