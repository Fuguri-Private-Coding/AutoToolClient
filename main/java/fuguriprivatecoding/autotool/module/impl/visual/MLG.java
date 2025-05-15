package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "MLG", category = Category.VISUAL)
public class MLG extends Module {

    boolean a;

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

                if (mc.thePlayer.ticksExisted % (!a ? 5 : 25 ) == 0) {
                    a = !a;
                }

                float uniform = 1f - (a ? -2 : 2.6f);

                mc.entityRenderer.getShaderGroup().listShaders.getFirst().getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
            } catch (Exception ignored) {}
        }
    }
}
