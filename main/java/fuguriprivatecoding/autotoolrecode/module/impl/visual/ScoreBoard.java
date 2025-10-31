package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.ScoreboardRenderEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.util.Collection;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Scoreboard", category = Category.VISUAL, description = "Позволяет изменять Scoreboard.")
public class ScoreBoard extends Module {

    public CheckBox remove = new CheckBox("Remove", this, true);

    Mode fonts = new Mode("Fonts", this, () -> !remove.isToggled());

    BooleanSupplier visible = () -> !remove.isToggled();

    public IntegerSetting posX = new IntegerSetting("Pos-X", this, visible, 0,100,0);
    public IntegerSetting posY = new IntegerSetting("Pos-Y", this, visible, 0,100,0);

    public FloatSetting scale = new FloatSetting("Scale", this, 0.1f, 2f, 1f, 0.1f);

    CheckBox roundedRect = new CheckBox("Rounded", this, visible);
    public FloatSetting roundFactor = new FloatSetting("Round Factor", this, () -> visible.getAsBoolean() && roundedRect.isToggled(),0, 10, 5, 0.1f);

    public final ColorSetting color = new ColorSetting("Color", this);

    public CheckBox glow = new CheckBox("Glow", this, visible);
    public CheckBox blur = new CheckBox("Blur", this, visible);

    BooleanSupplier shadow = () -> glow.isToggled();

    public final ColorSetting colorShadow = new ColorSetting("Shadow Color", this, shadow);

    public ScoreBoard() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof ScoreboardRenderEvent e) e.cancel();
        if (event instanceof Render2DEvent) {
            if (remove.isToggled()) return;

            ClientFontRenderer fontRenderer = Fonts.fonts.get(fonts.getMode());

            ScaledResolution sc = ScaleUtils.getScaledResolution(scale.getValue());

            GL11.glPushMatrix();
            GL11.glScaled(scale.getValue(), scale.getValue(), 1);

            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective != null) {
                Vector2f pos = getPos(sc);
                scoreboard = objective.getScoreboard();
                Collection<Score> collection = scoreboard.getSortedScores(objective);
                float width = 10.0F;

                for(Score score2 : collection) {
                    ScorePlayerTeam scoreplayerteam2 = scoreboard.getPlayersTeam(score2.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam2, score2.getPlayerName());
                    if (width < fontRenderer.getStringWidth(s1)) {
                        width = (float) fontRenderer.getStringWidth(s1 + "    ");
                    }
                }

                int height = collection.size() * 9 + 16;

                float finalWidth = width;
                if (roundedRect.isToggled()) {
                    if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RoundedUtils.drawRect(pos.x, pos.y, finalWidth, height, roundFactor.getValue(), colorShadow.getFadedColor()));
                    if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(pos.x, pos.y, finalWidth, height, roundFactor.getValue(), Color.WHITE));

                    StencilUtils.setUpTexture(pos.x, pos.y, finalWidth, height, roundFactor.getValue());
                    StencilUtils.writeTexture();

                    RoundedUtils.drawRect(pos.x, pos.y, finalWidth, height, roundFactor.getValue(), color.getFadedColor());
                    RoundedUtils.drawRect(pos.x, pos.y, finalWidth, 12, 0, color.getFadedColor());
                    fontRenderer.drawString(objective.getDisplayName(), (int) ((pos.x + finalWidth / 2f) - fontRenderer.getStringWidth(objective.getDisplayName()) / 2.0F), pos.y + 2.5f + 2, Color.WHITE);
                    StencilUtils.endWriteTexture();
                } else {
                    if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(pos.x, pos.y, finalWidth, height, 0, colorShadow.getColor(), colorShadow.getFadeColor(), colorShadow.getSpeed()));
                    if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(pos.x, pos.y, finalWidth, height, 0, colorShadow.getColor(), colorShadow.getFadeColor(), colorShadow.getSpeed()));

                    RoundedUtils.drawRect(pos.x, pos.y, width, height, 0, color.getFadedColor());
                    RoundedUtils.drawRect(pos.x, pos.y, width, 12, 0, color.getFadedColor());

                    fontRenderer.drawString(objective.getDisplayName(), (int) ((pos.x + width / 2f) - fontRenderer.getStringWidth(objective.getDisplayName()) / 2.0F), pos.y + 2.5f + 2, Color.WHITE);
                }
                int j = 0;

                for (Score score1 : collection) {
                    ++j;
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
                    fontRenderer.drawString(s1, pos.x + 3.0F, pos.y + height - (9 * j) + 2, Color.WHITE);
                }
            }
            GL11.glPopMatrix();
        }
    }

    Vector2f getPos(ScaledResolution sc) {
        return new Vector2f(
                (sc.getScaledWidth() / 100f) * this.posX.getValue(),
                (sc.getScaledHeight() / 100f) * this.posY.getValue()
        );
    }
}
