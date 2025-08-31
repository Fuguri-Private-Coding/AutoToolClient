package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.managers.CombatManager;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL, description = "Показывает информацию о противнике.")
public class TargetHUD extends Module {

    MultiMode render = new MultiMode("Render",this)
            .addModes("Health", "Background", "Head", "Name");

    CheckBox follow = new CheckBox("Follow", this);

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, follow::isToggled, -2,2,0,0.1f);

    IntegerSetting xPos = new IntegerSetting("X-Pos", this, () -> !follow.isToggled(),0, 100, 0);
    IntegerSetting yPos = new IntegerSetting("Y-Pos", this, () -> !follow.isToggled(),0, 100, 0);

    public final ColorSetting textColor = new ColorSetting("Text Color", this, () -> render.get("Name"));
    public final ColorSetting bgColor = new ColorSetting("Background Color", this, () -> render.get("Background"));

    FloatSetting bgRadius = new FloatSetting("Background Radius", this, 0f,200f,10f,0.1f);

    public final ColorSetting healthColor = new ColorSetting("Health Color", this, () -> render.get("Health"));

    FloatSetting headRadius = new FloatSetting("Head Radius", this, () -> render.get("Head"), 0f,50f,10f,0.1f);

    private final FloatSetting scale = new FloatSetting("Scale", this, 0.5f, 3f, 1.5f, 0.05f);

    CheckBox glow = new CheckBox("Glow", this);
    CheckBox blur = new CheckBox("Blur", this);

    public final ColorSetting bgShadowColor = new ColorSetting("Background Glow Color", this, () -> glow.isToggled());

    EntityLivingBase target;

    EasingAnimation currentScale = new EasingAnimation();
    EasingAnimation healthAnimation = new EasingAnimation();

    @EventTarget
    public void onEvent(Event event) {
        if (mc.currentScreen != null) return;

        if (event instanceof TickEvent) {
            CombatManager combatManager = Client.INST.getCombatManager();
            EntityLivingBase currentTarget = combatManager.getTarget();

            if (target == null && currentTarget != null) {
                target = currentTarget;
                currentScale.setEnd(1);
            } else if (target != null) {
                if (currentTarget == null) {
                    currentScale.setEnd(0);
                    if (!currentScale.isAnimating()) target = null;
                } else {
                    target = currentTarget;
                    currentScale.setEnd(1f);
                }
            }
        }

        if (target == null || target.getSkin() == null || target.getName() == null) return;

        if (event instanceof Render2DEvent e) {
            currentScale.update(5f, Easing.LINEAR);

            float width = 120;
            float height = 40;

            if (follow.isToggled()) {
                EntityRenderer entityRenderer = mc.entityRenderer;

                entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                float[] pos = Convertors.convert2D(
                        (float) (target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX),
                     (float) (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY) + target.height / 2f + yOffset.getValue(),
                        (float) (target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ), mc.gameSettings.guiScale);
                entityRenderer.setupOverlayRendering();

                if (pos == null || pos[2] > 1) return;

                pos[0] /= scale.getValue();
                pos[1] /= scale.getValue();

                float currentWidth = width * currentScale.getValue();
                float currentHeight = height * currentScale.getValue();

                float animX = (width / 2f) * currentScale.getValue();
                float animY = (height / 2f) * currentScale.getValue();

                float posX = pos[0] - animX;
                float posY = pos[1] - animY;

                ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");

                glScaled(this.scale.getValue(), this.scale.getValue(), 1);
                renderHUD(posX, posY, currentWidth, currentHeight, bgRadius.getValue() * currentScale.getValue(), font, target);
                glScaled(1f / this.scale.getValue(),1f / this.scale.getValue(),1f);
            } else {
                float posX = (e.getSc().getScaledWidth() / 100f) * xPos.getValue() / currentScale.getValue() - width / 2f;
                float posY = (e.getSc().getScaledHeight() / 100f) * yPos.getValue() / currentScale.getValue() - height / 2f;

                ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");

                double scale = this.scale.getValue() * currentScale.getValue();

                glScaled(scale, scale, 1f);
                renderHUD(posX, posY,  width, height, bgRadius.getValue(), font, target);
                glScaled(1f / scale,1f / scale,1f);
            }
        }
    }

    private void renderHUD(float posX, float posY, float width, float height, float radius, ClientFontRenderer font, EntityLivingBase target) {
        StencilUtils.renderStencil(
        () -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius,Color.BLACK, Color.BLACK, bgColor.getSpeed()),
            () -> {
                if (render.get("Background")) {
                    Color bgFadeColor = ColorUtils.fadeColor(bgColor.getColor(), bgColor.getFadeColor(), textColor.getSpeed());

                    RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius,bgFadeColor, bgFadeColor, bgColor.getSpeed());
                }

                if (render.get("Name")) {
                    font.drawString(target.getName(), posX + width / 2f + 18 - font.getStringWidth(target.getName()) / 2f, posY + height / 2f - 10f, ColorUtils.fadeColor(textColor.getColor(), textColor.getFadeColor(), textColor.getSpeed()));
                }

                if (render.get("Health")) {
                    float maxHealth = target.getMaxHealth();
                    float currentHealth = target.getHealth();
                    float healthPercentage = currentHealth / maxHealth;
                    float maxHealthBarWidth = width - height - 5;
                    float targetHealthWidth = maxHealthBarWidth * healthPercentage;

                    if (healthAnimation.getEnd() != targetHealthWidth) {
                        healthAnimation.setEnd(targetHealthWidth);
                    }
                    healthAnimation.update(5, Easing.LINEAR);

                    float animatedHealthWidth = healthAnimation.getValue();
                    RenderUtils.drawMixedRoundedRect(posX + height, posY + height / 2f + 2, maxHealthBarWidth, 10, 5, bgColor.getColor(),bgColor.getColor(), 180,0,0,90, bgColor.getSpeed());
                    RenderUtils.drawMixedRoundedRect(posX + height, posY + height / 2f + 2, animatedHealthWidth, 10, 5, healthColor.getColor(), healthColor.getFadeColor(), 180,0,0,90, healthColor.getSpeed());
                }

                if (render.get("Head") && target instanceof EntityPlayer) {
                    int hurtTime = target.hurtTime / 2;

                    StencilUtils.renderStencil(
                    () ->  RenderUtils.drawMixedRoundedRect(posX + 5, posY + 5, (height - 10 - hurtTime) * currentScale.getValue(), (height - 10 - hurtTime) * currentScale.getValue(), headRadius.getValue() * currentScale.getValue(),Color.WHITE, Color.WHITE, bgColor.getSpeed()),
                    () -> {
                        glColor4f(1f, 1f - hurtTime, 1f - hurtTime, 1f);
                        RenderUtils.quickDrawHead(target.getSkin(), posX + 5 + hurtTime / 2f, posY + 5 + hurtTime / 2f, (height - 10 - hurtTime) * currentScale.getValue(), (height - 10 - hurtTime) * currentScale.getValue());
                    });
                }
            }
        );

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius,bgShadowColor.getColor(), bgShadowColor.getFadeColor(), bgShadowColor.getSpeed()));
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius, Color.BLACK, Color.BLACK, bgColor.getSpeed()));
        }
    }
}