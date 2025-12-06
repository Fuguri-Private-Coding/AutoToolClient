package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderItemEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import lombok.Getter;

@ModuleInfo(name = "Animations", category = Category.VISUAL, description = "СВАГАВСКИЕ ХАЛЯЛЬ АНИМАЦИИ МЕЧА.")
public class Animations extends Module {

    @Getter
    static boolean animate;

    Mode mode = new Mode("Mode", this)
            .addModes("1.7", "Swong", "Sigma", "Sigma 2", "Scale", "Exhibition", "Exhibition2", "Spin", "Basic", "Slide")
            .setMode("1.7");

    FloatSetting X = new FloatSetting("X", this, -1f, 1f, 0f, 0.1f) {};
    FloatSetting Y = new FloatSetting("Y", this, -1f, 1f, 0.1f, 0.1f) {};
    FloatSetting Z = new FloatSetting("Z", this, -1f, 1f, 0.2f, 0.1f) {};

    FloatSetting scale = new FloatSetting("Scale", this, -1, 1f, 0f, 0.1f);

    public FloatSetting speed = new FloatSetting("Speed", this, 0.1f, 2f,1f,0.1f);

    public CheckBox always = new CheckBox("AlwaysBlocking", this, true);

    @Override
    public void onEvent(Event event) {
        if (event instanceof RenderItemEvent renderItemEvent) {
            float scales = 1 - scale.getValue();
            GL11.glScaled(scales, scales, scales);
            GlStateManager.translate(X.getValue(), Y.getValue(), Z.getValue());
            ItemRenderer itemRenderer = mc.getItemRenderer();
            float animationProgression = renderItemEvent.getEquipProgress();
            float convertedProgress = (float) Math.sin(Math.sqrt(renderItemEvent.getSwingProgress()) * Math.PI);
            float y;
            switch (mode.getMode()) {
                case "1.7" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression, renderItemEvent.getSwingProgress());
                    itemRenderer.doBlockTransformations();
                }

                case "Swong" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, renderItemEvent.getSwingProgress());
                    GlStateManager.rotate(convertedProgress * 30.0F / 2.0F, -convertedProgress, -0.0F, 9.0F);
                    GlStateManager.rotate(convertedProgress * 40.0F, 1.0F, -convertedProgress / 2.0F, -0.0F);
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Scale" -> {
                    GlStateManager.translate(0, 0, convertedProgress * -0.2);
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0f);
                    itemRenderer.doBlockTransformations();
                }

                case "Slide" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0f);
                    float var15 = MathHelper.sin(MathHelper.sqrt_float(renderItemEvent.getSwingProgress()) * 3.1415927F);
                    GlStateManager.rotate(-var15 * 55.0F / 2.0F, -8.0F, -0.0F, 9.0F);
                    GlStateManager.rotate(-var15 * 45.0F, 1.0F, var15 / 2.0F, -0.0F);
                    itemRenderer.doBlockTransformations();
                    GL11.glTranslated(1.2D, 0.3D, 0.5D);
                    GL11.glTranslatef(-1.0F, mc.thePlayer.isSneaking() ? -0.1F : -0.2F, 0.2F);
                    GlStateManager.scale(1.2F, 1.2F, 1.2F);
                }

                case "Basic" -> {
                    itemRenderer.transformFirstPersonItem(-0.25F, 1.0F + convertedProgress / 10.0F);
                    GL11.glRotated((-convertedProgress * 25.0F), 1.0, 0.0, 0.0);
                    itemRenderer.doBlockTransformations();
                }

                case "Spin" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    GlStateManager.translate(0.0F, 0.2F, -1.0F);
                    GlStateManager.rotate(-59.0F, -1.0F, 0.0F, 3.0F);
                    GlStateManager.rotate((float)(-(System.currentTimeMillis() / 2L % 360L)), 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                }

                case "Exhibition2" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, convertedProgress / 1.2F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Sigma" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    y = -convertedProgress * 2.0F;
                    GlStateManager.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                    GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(250.0F, 0.2F, 1.0F, -0.6F);
                    GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                    GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                }

                case "Exhibition" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, convertedProgress / 1.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Sigma 2" -> {
                    itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                    GlStateManager.scale(0.8F, 0.8F, 0.8F);
                    GlStateManager.translate(0.0F, 0.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                    GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0F, 1.0F, 0.0F);
                }
            }
            GL11.glScaled(1f / scales,1f / scales, 1f / scales);
        }
    }
}

