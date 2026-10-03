package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StencilShader implements Imports {

    private final Shader shader = Shaders.stencil;

    public void setUpTexture(float x, float y, float width, float height, float radius) {


    }

    public void startWrite() {

    }

    public void stopWrite() {

    }
}
