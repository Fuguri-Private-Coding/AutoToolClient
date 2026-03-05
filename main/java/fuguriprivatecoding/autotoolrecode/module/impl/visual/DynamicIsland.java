package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RectUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm");

    EasingAnimation x, y, width, height, textAlpha;

    String currentText, lastText;

    Date date = new Date();

    public DynamicIsland() {
        x = new EasingAnimation();
        y = new EasingAnimation();
        width = new EasingAnimation();
        height = new EasingAnimation();
        textAlpha = new EasingAnimation();
    }

    @Override
    public void onEvent(Event event) {
        ClientFont font = Fonts.fonts.get("SFPro");

        if (event instanceof Render2DEvent) {
            ScaledResolution sc = ScaleUtils.getScaledResolution();

            float additionalHeight = 15;

            updateText(Client.INST.getFullName());

            float textWidth = font.getStringWidth(lastText);

            x.setEnd(sc.getScaledWidth() / 2f - (textWidth + 10) / 2f);
            y.setEnd(5);
            width.setEnd(textWidth + 10);
            height.setEnd(additionalHeight);

            x.update(3, Easing.OUT_CUBIC);
            y.update(3, Easing.OUT_CUBIC);
            width.update(3, Easing.OUT_CUBIC);
            height.update(3, Easing.OUT_CUBIC);
            textAlpha.update(6, Easing.OUT_CUBIC);

            float x = this.x.getValue();
            float y = this.y.getValue();
            float width = this.width.getValue();
            float height = this.height.getValue();

            date.setTime(System.currentTimeMillis());

            String currentTimeText = FORMAT.format(date);

            float timeWidth = font.getStringWidth(currentTimeText);

            float timeX = x - timeWidth - 3;
            float timeY = y + 5;

            float textX = x + 5;
            float textY = y + 5;

            RenderUtils.drawRoundedOutLineRectangle(x, y, width, height, 7.5f, Colors.BLACK.withAlpha(0.5f), Colors.WHITE.withAlpha(0.3f), Colors.WHITE.withAlpha(0.3f));

            BloomUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(x, y, width, height, 7.5f, Color.BLACK, Color.WHITE, 3));

            font.drawString(currentText, textX, textY, Colors.WHITE.withAlphaClamp(textAlpha.getValue()));
            font.drawString(currentTimeText, timeX, timeY, Color.WHITE);

            float internetX = x + width + 5;
            float internetY = y + 5;

            RoundedUtils.drawRect(internetX, internetY + 2, 0.5f, 2, 0, Colors.WHITE);
            RoundedUtils.drawRect(internetX + 2.5f, internetY + 1, 0.5f, 3, 0, Colors.WHITE);
            RoundedUtils.drawRect(internetX + 2.5f + 2.5f, internetY, 0.5f, 4, 0, Colors.WHITE);
        }
    }

    private void updateText(String text) {
        if (!Objects.equals(currentText, text)) {
            lastText = text;
        }

        if (textAlpha.getValue() == 0f && !width.isAnimating()) {
            currentText = lastText;
        }

        textAlpha.setEnd(Objects.equals(currentText, lastText));
    }
}
