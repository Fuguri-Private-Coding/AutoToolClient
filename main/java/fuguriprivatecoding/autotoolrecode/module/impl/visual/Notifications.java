package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.awt.*;

@ModuleInfo(name = "Notifications", category = Category.VISUAL, description = "Показывает информаци и включении/выключении модулей.")
public class Notifications extends Module {

    final Mode fonts = new Mode("Fonts", this);

    final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this);

    public final FloatSetting removeTime = new FloatSetting("RemoveTime", this, 100, 1500, 250, 50);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    final CheckBox blur = new CheckBox("Blur", this);

    private static final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void addNotification(String name, boolean toggled) {
        if (!Modules.getModule(Notifications.class).isToggled()) return;

        Notification notification = new Notification(name, toggled, System.currentTimeMillis(), (long) Modules.getModule(Notifications.class).removeTime.getValue());
        notification.getOpenAnim().setEnd(1);

        notifications.add(notification);
    }

    public Notifications() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRegular");
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            notifications.removeIf(notification -> notification.isDelete() && notification.getOpenAnim().getValue() == 0);

            ScaledResolution sc = new ScaledResolution(mc);
            ClientFontRenderer fontRenderer = Fonts.fonts.get(fonts.getMode());

            float yOffset = 0;
            for (Notification notification : notifications) {
                String text = "§f[§9AutoTool§f] " + "§fМодуль " + notification.getName() + "§f был" + (notification.isToggled() ? "§a включен" : "§c выключен") + "§f.";

                EasingAnimation openAnim = notification.getOpenAnim();

                openAnim.update(2f, Easing.OUT_BACK);

                if (notification.isDelete()) openAnim.setEnd(0);

                float width = fontRenderer.getStringWidth(text);

                float x = sc.getScaledWidth() / 2f - width / 2f;
                float y = 5 + yOffset;
                float height = 15;

                ScaleUtils.startScaling(x, y, width + 5, height, openAnim.getValue());

                Color textColor = Colors.WHITE.withAlphaClamp(openAnim.getValue());
                Color backgroundColor = new Colors(this.backgroundColor.getFadedColor()).withMultiplyAlphaClamp(openAnim.getValue());
                Color backgroundGlowColorFirst = new Colors(this.glowColor.getColor()).withAlphaClamp(openAnim.getValue());
                Color backgroundGlowColorSecond = new Colors(this.glowColor.getFadeColor()).withAlphaClamp(openAnim.getValue());

                if (glow.isToggled()) {
                    BloomUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(x, y, width + 5, height, 7.5f, backgroundGlowColorFirst, backgroundGlowColorSecond, glowColor.getSpeed()));
                }

                if (blur.isToggled()) {
                    BlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(x, y, width + 5, height, 7.5f, backgroundGlowColorFirst, backgroundGlowColorSecond, glowColor.getSpeed()));
                }

                RoundedUtils.drawRect(x, y, width + 5, height, 7.5f, backgroundColor);
                fontRenderer.drawString(text, x + 5, y + 5f, textColor);

                ScaleUtils.stopScaling();
                yOffset += 20 * openAnim.getValue();
            }
        }
    }
}
