package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "MotionBlur", category = Category.VISUAL)
public class MotionBlur extends Module {

    IntegerSetting blurAmount = new IntegerSetting("BlurAmount", this, 1, 9, 7);

    @Override
    public void onEnable() {
        mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
    }

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            try {
                if (mc.entityRenderer.getShaderGroup() == null) mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));

                float uniform = 1f - blurAmount.getValue() / 10f;

                mc.entityRenderer.getShaderGroup().listShaders.getFirst().getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
            } catch (Exception ignored) {}
        }
    }
}
