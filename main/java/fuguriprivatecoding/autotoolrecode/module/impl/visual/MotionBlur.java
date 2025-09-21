package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "MotionBlur", category = Category.VISUAL, description = "Размытие экрана при движение камерой.")
public class MotionBlur extends Module {

    public IntegerSetting blurAmount = new IntegerSetting("BlurAmount", this, 1, 100, 70);

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            try {
                if (mc.entityRenderer.getShaderGroup() == null) mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));

                float uniform = 1f - blurAmount.getValue() / 100f;

                mc.entityRenderer.getShaderGroup().listShaders.getFirst().getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
            } catch (Exception ignored) {}
        }
    }
}
