package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connection.BackTrack;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    Mode fonts = new Mode("Fonts", this)
            .addModes("MuseoSans", "Roboto", "JetBrains", "SFPro")
            .setMode("MuseoSans")
            ;

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, 0, 100, 5, 0.1f);

    FloatSetting width = new FloatSetting("Width", this, 0f,50f,10f,0.1f);
    FloatSetting animationSpeed = new FloatSetting("Animation Speed", this, 0f,50f,15f,0.1f);

    CheckBox textFade = new CheckBox("Text Fade", this, false);
    ColorSetting textColor1 = new ColorSetting("Text Color1", this, 1,1,1,1);
    ColorSetting textColor2 = new ColorSetting("Text Color2", this,() -> textFade.isToggled(), 0,0,0,1);
    FloatSetting textSpeed = new FloatSetting("Text Speed", this,() -> textFade.isToggled(),0.1f, 20, 1, 0.1f);

    FloatSetting bgRadius = new FloatSetting("Background Radius", this,0f, 20, 7.5f, 0.1f);
    CheckBox bgFade = new CheckBox("Background Fade", this, false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> bgFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox glow = new CheckBox("Glow", this, true);
    CheckBox blur = new CheckBox("Blur", this, true);

    CheckBox bgFadeShadow = new CheckBox("Background Fade Shadow", this, () -> glow.isToggled(), false);
    ColorSetting bgColor1Shadow = new ColorSetting("Background Color1 Shadow", this, () -> glow.isToggled(), 0,0,0,1);
    ColorSetting bgColor2Shadow = new ColorSetting("Background Color2 Shadow", this,() -> glow.isToggled() && bgFadeShadow.isToggled(), 0,0,0,1);
    FloatSetting bgSpeedShadow = new FloatSetting("Background Speed Shadow", this,() -> glow.isToggled() && bgFadeShadow.isToggled(),0.1f, 20, 1, 0.1f);

    Color fadeTextColor;
    Color fadeBackgroundColor;

    Animation2D currentWidth = new Animation2D();
    Animation2D currentHeight = new Animation2D();
    Animation2D size = new Animation2D();
    Animation2D needY = new Animation2D();
    String currentText;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ClientFontRenderer font = Client.INST.getFonts().fonts.get(fonts.getMode());
            Vector2f screenSize = new Vector2f(e.getSc().getScaledWidth(), e.getSc().getScaledHeight());

            updateColors();

            float height = 15;
            float yOffset = this.yOffset.getValue();

            EntityLivingBase ent = Client.INST.getCombatManager().getTarget();
            BackTrack backTrack = Client.INST.getModuleManager().getModule(BackTrack.class);
            Scaffold scaffOld = Client.INST.getModuleManager().getModule(Scaffold.class);

            String name = Client.INST.getFullName();
            String bps = String.format("%.3f", mc.thePlayer.getBps(false));
            String target = "Current Target - Undefined";
            String scaffold = "Block Left - Undefined";
            String backtrack = "Distance - Undefined";

            currentHeight.endY = height;

            currentText = name;
            needY.endY = yOffset;

            if (MoveUtils.isMoving()) {
                currentText = bps;
                resetNeedY(15, yOffset);
            }

            if (ent != null) {
                target = "Current Target - " + ent.getName();
                currentText = target;
                resetNeedY(30, yOffset);
            }

            if (mc.thePlayer.inventory.getCurrentItem() != null) {
                if (scaffOld.isToggled() && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
                    scaffold = "Blocks Left - " + mc.thePlayer.inventory.getCurrentItem().stackSize;
                    currentText = scaffold + (MoveUtils.isMoving() ? ", " + bps : "");
                    resetNeedY(45, yOffset);
                }
            }

            if (backTrack.isToggled() && backTrack.packetBuffer.size() > 10 && ent != null) {
                AxisAlignedBB realBox = ent.getEntityBoundingBox().offset(ent.nx - ent.posX, ent.ny - ent.posY, ent.nz - ent.posZ).expand(
                        ent.getCollisionBorderSize(),
                        ent.getCollisionBorderSize(),
                        ent.getCollisionBorderSize()
                );

                backtrack = "Distance - " + String.format("%.1f", DistanceUtils.getDistance(realBox));
                currentText = backtrack;
                resetNeedY(60, yOffset);
            }

            currentWidth.endX = (float) (width.getValue() + font.getStringWidth(currentText));

            updateAnimations();

            String finalTarget = target;
            String finalScaffold = scaffold;
            String finalBacktrack = backtrack;

            StencilUtils.renderStencil(
                    () -> RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.x / 2f, 4.5f + yOffset, currentWidth.x, currentHeight.y, bgRadius.getValue(), Color.BLACK, Color.BLACK, 1f),
                    () -> {
                        RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.x / 2f, 4.5f + yOffset, currentWidth.x, currentHeight.y, bgRadius.getValue(), fadeBackgroundColor, fadeBackgroundColor, bgSpeed.getValue());

                        font.drawString(name, (screenSize.x / 2f - font.getStringWidth(name) / 2f), 10 + needY.y,fadeTextColor);
                        font.drawString(bps, (screenSize.x / 2f - font.getStringWidth(bps) / 2f), 25 + needY.y,fadeTextColor);
                        font.drawString(finalTarget, (screenSize.x / 2f - font.getStringWidth(finalTarget) / 2f), 40 + needY.y, fadeTextColor);
                        font.drawString(finalScaffold, (screenSize.x / 2f - font.getStringWidth(finalScaffold) / 2f), 55 + needY.y, fadeTextColor);
                        font.drawString(finalBacktrack, (screenSize.x / 2f - font.getStringWidth(finalBacktrack) / 2f), 70 + needY.y, fadeTextColor);
                    }
            );

            if (glow.isToggled()) {
                BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.x / 2f, 4.5f + yOffset, currentWidth.x, height, bgRadius.getValue(), bgColor1Shadow.getColor(), bgColor2Shadow.getColor(), bgSpeedShadow.getValue()));
            }

            if (blur.isToggled()) {
                GaussianBlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.x / 2f, 4.5f + yOffset, currentWidth.x, height, bgRadius.getValue(), Color.WHITE, Color.WHITE, bgSpeedShadow.getValue()));
            }
        }
    }

    private void updateAnimations() {
        currentWidth.update(animationSpeed.getValue());
        size.update(animationSpeed.getValue());
        needY.update(animationSpeed.getValue());
        currentHeight.update(animationSpeed.getValue());
    }

    private void updateColors() {
        fadeTextColor = textFade.isToggled() ? ColorUtils.fadeColor(
                textColor1.getColor(), textColor2.getColor(), textSpeed.getValue()) : textColor1.getColor();

        fadeBackgroundColor = bgFade.isToggled() ? ColorUtils.fadeColor(
                bgColor1.getColor(), bgColor2.getColor(), bgSpeed.getValue()) : bgColor1.getColor();
    }

    public void resetNeedY(float y, float yOffset) {
        needY.endY = yOffset;
        needY.endY -= y;
    }
}
