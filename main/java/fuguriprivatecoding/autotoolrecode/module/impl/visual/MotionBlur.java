package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.MBlurEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.MotionBlurUtils;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "MotionBlur", category = Category.VISUAL, description = "Размытие при движение.")
public class MotionBlur extends Module {

    Mode blurMode = new Mode("BlurMode", this)
        .addModes("Phosphor", "Shader")
        .setMode("Shader")
        ;

    public IntegerSetting blurAmount = new IntegerSetting("BlurAmount", this, 1, 100, 70);

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    @Override
    public void onEvent(Event event) {
        switch (blurMode.getMode()) {
            case "Phosphor" -> {
                if (event instanceof TickEvent) {
                    try {
                        if (mc.entityRenderer.getShaderGroup() == null)
                            mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));

                        float uniform = 1f - blurAmount.getValue() / 100f;

                        mc.entityRenderer.getShaderGroup().listShaders.getFirst().getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
                    } catch (Exception ignored) { }
                }
            }

            case "Shader" -> {
                if (event instanceof RunGameLoopEvent) {
                    MotionBlurUtils.inputFramebuffer.bindFramebuffer(true);
                }

                if (event instanceof MBlurEvent) {
                    MotionBlurUtils.inputFramebuffer.unbindFramebuffer();
                    MotionBlurUtils.draw();
                }
            }
        }
    }
}
