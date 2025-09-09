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
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL, description = "Показывает информацию о противнике.")
public class TargetHUD extends Module {

    MultiMode render = new MultiMode("Render",this)
            .addModes("Health", "Background", "Head", "Name");

    Mode fonts = new Mode("Fonts", this, () -> render.get("Name"))
            .setMode("SFProRounded")
            ;

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

    Vector2f pos = new Vector2f();

    public TargetHUD() {
        Client.INST.getFonts().fonts.forEach((fontName, fontRenderer) -> {
            fonts.addMode(fontName);
        });
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.currentScreen != null) return;

        if (event instanceof TickEvent) {
            updateTarget();
        }

        if (target == null || target.getSkin() == null || target.getName() == null) return;

        if (event instanceof Render2DEvent e) {
            currentScale.update(5f, Easing.IN_OUT_QUINT);

            ClientFontRenderer font = Client.INST.getFonts().fonts.get(fonts.getMode());

            float width = 120;
            float height = 40;

            if (follow.isToggled()) {
                EntityRenderer entityRenderer = mc.entityRenderer;

                entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                float[] positions = Convertors.convert2D(
                        (float) (target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX),
                        (float) (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY) + target.height / 2f + yOffset.getValue(),
                        (float) (target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ), mc.gameSettings.guiScale);
                entityRenderer.setupOverlayRendering();

                if (positions == null || positions[2] > 1) return;

                positions[0] /= scale.getValue();
                positions[1] /= scale.getValue();

                float currentWidth = width * currentScale.getValue();
                float currentHeight = height * currentScale.getValue();

                pos.set(
                        positions[0] - ((width / 2f) * currentScale.getValue()),
                        positions[1] - ((height / 2f) * currentScale.getValue())
                );

                renderHUD(pos.x, pos.y, currentWidth, currentHeight, bgRadius.getValue() * currentScale.getValue(), font, target, this.scale.getValue());
            } else {
                pos.set(
                        (e.getSc().getScaledWidth() / 100f) * xPos.getValue() / currentScale.getValue() - width / 2f,
                        (e.getSc().getScaledHeight() / 100f) * yPos.getValue() / currentScale.getValue() - height / 2f
                );

                double scale = this.scale.getValue() * currentScale.getValue();

                renderHUD(pos.x, pos.y, width, height, bgRadius.getValue(), font, target, scale);
            }
        }
    }

    private void renderHUD(float posX, float posY, float width, float height, float radius, ClientFontRenderer font, EntityLivingBase target, double scaleFactor) {
        glScaled(scaleFactor, scaleFactor, 1);
        StencilUtils.renderStencil(
        () -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius,Color.BLACK, Color.BLACK, bgColor.getSpeed()),
            () -> {
                if (render.get("Background")) RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius, bgColor.getFadedColor(), bgColor.getFadedColor(), bgColor.getSpeed());
                if (render.get("Name")) font.drawString(target.getName(), posX + width / 2f + 18 - font.getStringWidth(target.getName()) / 2f, posY + height / 2f - 10f, textColor.getFadedColor());

                if (render.get("Health")) {
                    float maxHealth = target.getMaxHealth();
                    float currentHealth = target.getHealth();
                    float healthPercentage = currentHealth / maxHealth;
                    float maxHealthBarWidth = width - height - 5;
                    float targetHealthWidth = maxHealthBarWidth * healthPercentage;

                    if (healthAnimation.getEnd() != targetHealthWidth) healthAnimation.setEnd(targetHealthWidth);

                    healthAnimation.update(7, Easing.LINEAR);

                    float animatedHealthWidth = healthAnimation.getValue();

                    RenderUtils.drawMixedRoundedRect(posX + height, posY + height / 2f + 2, maxHealthBarWidth, 10, 5, bgColor.getColor(),bgColor.getColor(), bgColor.getSpeed());
                    RenderUtils.drawMixedRoundedRect(posX + height, posY + height / 2f + 2, animatedHealthWidth, 10, 5, healthColor.getColor(), healthColor.getFadeColor(), 180,0,0,90, healthColor.getSpeed());
                }

                if (render.get("Head") && target instanceof EntityPlayer) {
                    int hurtTime = target.hurtTime / 2;

                    StencilUtils.renderStencil(
                    () -> RenderUtils.drawMixedRoundedRect(posX + 5 + hurtTime / 2f, posY + 5 + hurtTime / 2f, (height - 10 - hurtTime) * currentScale.getValue(), (height - 10 - hurtTime) * currentScale.getValue(), headRadius.getValue() * currentScale.getValue(), Color.WHITE, Color.WHITE, bgColor.getSpeed()),
                    () -> {
                        glColor4f(1f, 1f - hurtTime, 1f - hurtTime, 1f);
                        RenderUtils.quickDrawHead(target.getSkin(), posX + 5 + hurtTime / 2f, posY + 5 + hurtTime / 2f, (height - 10 - hurtTime) * currentScale.getValue(), (height - 10 - hurtTime) * currentScale.getValue());
                    });
                }
            }
        );

        if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius,bgShadowColor.getColor(), bgShadowColor.getFadeColor(), bgShadowColor.getSpeed()));
        if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(posX,posY,width,height,radius, Color.BLACK, Color.BLACK, bgColor.getSpeed()));

        glScaled(1f / scaleFactor,1f / scaleFactor,1f);
    }

    private void updateTarget() {
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
}