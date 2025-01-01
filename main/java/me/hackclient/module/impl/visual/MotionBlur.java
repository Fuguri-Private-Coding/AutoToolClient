package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(name = "MotionBlur", category = Category.VISUAL, toggled = true)
public class MotionBlur extends Module {

    IntegerSetting blurAmount = new IntegerSetting("BlurAmount", this, 1, 9, 5);

    @Override
    public void onEnable() {
        mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "hackclient/shaders/post/motion_blur.json"));
    }

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

        if (event instanceof TickEvent) {
            if (mc.theWorld == null) return;

            try {
                float uniform = 1f - (blurAmount.getValue() / 10f);

                mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
            } catch (Exception ignored) {
            }
        }
    }
}
