package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.RenderItemEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;

@ModuleInfo(name = "Animations", category = Category.VISUAL)
public class Animations extends Module {

    static boolean animate;

    ModeSetting mode = new ModeSetting("Mode", this, "Sigma 2", new String[]{
            "Sigma",
            "Sigma 2",
            "Exhibition",
            "Chill",
    });

    FloatSettings X = new FloatSettings("X", this, -1f, 1f, 0f, 0.1f);
    FloatSettings Y = new FloatSettings("X", this, -1f, 1f, 0f, 0.1f);
    FloatSettings Z = new FloatSettings("Z", this, -1f, 1f, 0f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof RenderItemEvent renderItemEvent) {
            GlStateManager.translate(X.getValue(), Y.getValue(), Z.getValue());
            ItemRenderer itemRenderer = mc.getItemRenderer();
            float animationProgression = renderItemEvent.getEquipProgress();
            float convertedProgress = (float) Math.sin(Math.sqrt(renderItemEvent.getSwingProgress()) * Math.PI);
            float y;
            switch (mode.getMode()) {
                case "Chill":
                    itemRenderer.transformFirstPersonItem(animationProgression / 1.5F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    GlStateManager.translate(-0.05F, 0.3F, 0.3F);
                    GlStateManager.rotate(-convertedProgress * 140.0F, 8.0F, 0.0F, 8.0F);
                    GlStateManager.rotate(convertedProgress * 90.0F, 8.0F, 0.0F, 8.0F);
                    break;
                case "Sigma":
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    y = -convertedProgress * 2.0F;
                    GlStateManager.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                    GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(250.0F, 0.2F, 1.0F, -0.6F);
                    GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                    GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                    break;
                case "Exhibition":
                    itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, convertedProgress / 1.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    break;
                case "Sigma 2":
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    GlStateManager.scale(0.8F, 0.8F, 0.8F);
                    GlStateManager.translate(0.0F, 0.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                    GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0F, 1.0F, 0.0F);
            }
        }
    }

    public static boolean isAnimate() {
        return animate;
    }

    public static void setAnimate(boolean animate) {
        Animations.animate = animate;
    }
}

