package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderItemEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Hand", category = Category.VISUAL, description = "Делает манипуляции с рукой и свага анимации.")
public class Hand extends Module {

    public final MultiMode effect = new MultiMode("Effect", this)
        .add("Animations", true)
        .add("ItemPos", true)
        ;

    BooleanSupplier animationsSupplier = () -> effect.get("Animations");
    BooleanSupplier itemPosSupplier = () -> effect.get("ItemPos");

    Mode mode = new Mode("Mode", this, animationsSupplier)
        .addModes("1.7", "Swong", "Sigma", "Sigma 2", "Scale", "Exhibition", "Exhibition2", "Basic", "Slide")
        .setMode("1.7");

    FloatSetting scale = new FloatSetting("Scale", this, animationsSupplier, -1, 1f, 0.1f, 0.01f);
    public FloatSetting speed = new FloatSetting("Speed", this, animationsSupplier, 0.1f, 2f,1f,0.01f);
    public CheckBox always = new CheckBox("AlwaysBlocking", this, animationsSupplier, true);

    public FloatSetting x = new FloatSetting("X", this, itemPosSupplier, -2,2,-0.15f,0.01f);
    public FloatSetting y = new FloatSetting("Y", this, itemPosSupplier, -2,2,0,0.01f);
    public FloatSetting z = new FloatSetting("Z", this, itemPosSupplier, -2,2,0,0.01f);

    public FloatSetting rotateX = new FloatSetting("RotateX", this, itemPosSupplier, -90,90,0,0.1f);
    public FloatSetting rotateY = new FloatSetting("RotateY", this, itemPosSupplier, -90,90,0,0.1f);
    public FloatSetting rotateZ = new FloatSetting("RotateZ", this, itemPosSupplier, -90,90,0,0.1f);

    public FloatSetting blockX = new FloatSetting("BlockX", this, itemPosSupplier, -2,2,0,0.01f);
    public FloatSetting blockY = new FloatSetting("BlockY", this, itemPosSupplier, -2,2,-0.1f,0.01f);
    public FloatSetting blockZ = new FloatSetting("BlockZ", this, itemPosSupplier, -2,2,-0.2f,0.01f);

    public FloatSetting blockRotateX = new FloatSetting("BlockRotateX", this, itemPosSupplier, -90,90,0,0.1f);
    public FloatSetting blockRotateY = new FloatSetting("BlockRotateY", this, itemPosSupplier, -90,90,0,0.1f);
    public FloatSetting blockRotateZ = new FloatSetting("BlockRotateZ", this, itemPosSupplier, -90,90,0,0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof RenderItemEvent renderItemEvent) {
            float scale = 1 - this.scale.getValue();
            float resetScale = 1f / scale;

            GL11.glScaled(scale, scale, scale);
            ItemRenderer itemRenderer = mc.getItemRenderer();

            float equipProgress = renderItemEvent.getEquipProgress();
            float convertedProgress = (float) Math.sin(Math.sqrt(renderItemEvent.getSwingProgress()) * Math.PI);

            float y = -convertedProgress * 2.0F;
            switch (mode.getMode()) {
                case "1.7" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress, renderItemEvent.getSwingProgress());
                    itemRenderer.doBlockTransformations();
                }

                case "Swong" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress / 2.0F, renderItemEvent.getSwingProgress());
                    GlStateManager.rotate(convertedProgress * 30.0F / 2.0F, -convertedProgress, -0.0F, 9.0F);
                    GlStateManager.rotate(convertedProgress * 40.0F, 1.0F, -convertedProgress / 2.0F, -0.0F);
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Scale" -> {
                    GlStateManager.translate(0, 0, convertedProgress * -0.2);
                    itemRenderer.transformFirstPersonItem(equipProgress, 0.0f);
                    itemRenderer.doBlockTransformations();
                }

                case "Slide" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress, 0.0f);
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

                case "Exhibition2" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, convertedProgress / 1.2F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Sigma" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress, 0.0F);
                    GlStateManager.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                    GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(250.0F, 0.2F, 1.0F, -0.6F);
                    GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                    GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                }

                case "Exhibition" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress / 2.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.3F, -0.0F);
                    GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                    GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, convertedProgress / 1.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                }

                case "Sigma 2" -> {
                    itemRenderer.transformFirstPersonItem(equipProgress, 0.0F);
                    GlStateManager.scale(0.8F, 0.8F, 0.8F);
                    GlStateManager.translate(0.0F, 0.1F, 0.0F);
                    itemRenderer.doBlockTransformations();
                    GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                    GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0F, 1.0F, 0.0F);
                }
            }
            GL11.glScaled(resetScale,resetScale, resetScale);
        }
    }
}
