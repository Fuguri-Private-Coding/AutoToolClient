package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.RenderItemEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name = "Animations", category = Category.VISUAL, toggled = true)
public class Animations extends Module {

    static boolean animate;

    ModeSetting mode = new ModeSetting("Mode", this, "Sigma", new String[]{
            "Sigma",
            "Sigma 2",
            "Scale",
            "Exhibition",
            "Chill",
            "Gothaj",
            "Spin",
            "Basic",
            "Slide",
    });

    FloatSetting X = new FloatSetting("X", this, -1f, 1f, -0.1f, 0.1f) {};
    FloatSetting Y = new FloatSetting("Y", this, -1f, 1f, 0.2f, 0.1f) {};
    FloatSetting Z = new FloatSetting("Z", this, -1f, 1f, -0.1f, 0.1f) {};

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
                case "Scale":
                    GlStateManager.translate(0, 0, convertedProgress * -0.2);
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0f);
                    itemRenderer.doBlockTransformations();
                    break;

                case "Slide":
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0f);
                    float var15 = MathHelper.sin(MathHelper.sqrt_float(renderItemEvent.getSwingProgress()) * 3.1415927F);
                    GlStateManager.rotate(-var15 * 55.0F / 2.0F, -8.0F, -0.0F, 9.0F);
                    GlStateManager.rotate(-var15 * 45.0F, 1.0F, var15 / 2.0F, -0.0F);
                    itemRenderer.doBlockTransformations();
                    GL11.glTranslated(1.2D, 0.3D, 0.5D);
                    GL11.glTranslatef(-1.0F, mc.thePlayer.isSneaking() ? -0.1F : -0.2F, 0.2F);
                    GlStateManager.scale(1.2F, 1.2F, 1.2F);
                    break;
                case "Basic":
                    itemRenderer.transformFirstPersonItem(-0.25F, 1.0F + convertedProgress / 10.0F);
                    GL11.glRotated((-convertedProgress * 25.0F), 1.0, 0.0, 0.0);
                    itemRenderer.doBlockTransformations();
                    break;
                case "Spin":
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    GlStateManager.translate(0.0F, 0.2F, -1.0F);
                    GlStateManager.rotate(-59.0F, -1.0F, 0.0F, 3.0F);
                    GlStateManager.rotate((float)(-(System.currentTimeMillis() / 2L % 360L)), 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                    break;
                case "Gothaj":
                    itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, convertedProgress / 1.2F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    break;
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

