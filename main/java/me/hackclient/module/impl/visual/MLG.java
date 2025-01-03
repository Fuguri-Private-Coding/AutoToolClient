package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.GameTickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
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

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof GameTickEvent) {
            try {
                if (mc.entityRenderer.getShaderGroup() == null) mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));

                if (mc.thePlayer.ticksExisted % (!a ? 5 : 25 ) == 0) {
                    a = !a;
                }

                float uniform = 1f - (a ? -2 : 2.6f);

                mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0f, 0f);
            } catch (Exception ignored) {

            }
        }
    }

}
