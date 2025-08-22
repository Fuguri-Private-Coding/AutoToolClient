package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Notifications", category = Category.VISUAL)
public class Notifications extends Module {

    Mode fonts = new Mode("Fonts", this)
            .addModes("MuseoSans", "Roboto", "JetBrains")
            .setMode("MuseoSans");

    Mode position = new Mode("Position", this)
            .addModes("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right")
            .setMode("Bottom-Right");

    FloatSetting height = new FloatSetting("Height", this, 15,50,30,0.1f);
    FloatSetting width = new FloatSetting("Width", this, 100,200,150,0.1f);

    FloatSetting margin = new FloatSetting("Margin", this, 0,10,5,0.1f);

    IntegerSetting displayTime = new IntegerSetting("Display Time", this, 500,5000,1500);

    public final ColorSetting textColor = new ColorSetting("Text Color", this);

    public final ColorSetting textDisableColor = new ColorSetting("Text Disable Color", this);

    public final ColorSetting textEnableColor = new ColorSetting("Text Enable Color", this);

    public final ColorSetting bgColor = new ColorSetting("Background Color", this);

    IntegerSetting bgRadius = new IntegerSetting("Background Radius", this, 1,50,10);

    public CheckBox glow = new CheckBox("Glow", this);
    public CheckBox blur = new CheckBox("Blur", this);

    BooleanSupplier shadow = () -> glow.isToggled();

    public final ColorSetting bgColorShadow = new ColorSetting("Background Shadow Color", this, shadow);

    CheckBox line = new CheckBox("Line",this, true);
    FloatSetting lineSize = new FloatSetting("Line Size", this,() -> line.isToggled(),0f, 10, 1, 0.1f);
    public final ColorSetting lineColor = new ColorSetting("Line Color", this);

    ClientFontRenderer font = Client.INST.getFonts().fonts.get("JetBrains");

    public static Notifications instance;
    public final List<Notification> notificationList = new CopyOnWriteArrayList<>();
    private final StopWatch timer = new StopWatch();

    Color bgColors, lineColors, textColors, textDisableColors, textEnableColors;

    public Notifications() {
        instance = this;
    }

    public void addNotification(String moduleName, boolean toggle) {
        notificationList.addFirst(new Notification(moduleName, toggle, System.currentTimeMillis()));
        timer.reset();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (!font.name.equalsIgnoreCase(fonts.getMode())) font = Client.INST.getFonts().fonts.get(fonts.getMode());
        if (event instanceof Render2DEvent e) {
            renderNotifications(e.getSc().getScaledWidth(), e.getSc().getScaledHeight());
        }
    }

    private void renderNotifications(int screenWidth, int screenHeight) {
        if (notificationList.isEmpty()) return;
        for (int i = 0; i < notificationList.size(); i++) {
            Notification notification = notificationList.get(i);

            Vector4f pos = calculatePosition(screenWidth, screenHeight, i, width.getValue(), height.getValue(), margin.getValue());

            float visibility = calculateVisibility(notification, displayTime.getValue());
            if (visibility <= 0) {
                notificationList.remove(notification);
                continue;
            }

            Animation2D animation = createAnimation(pos.x, pos.y, pos.z, pos.w, screenWidth, screenHeight, width.getValue(), height.getValue());
            applyAnimationStyle(animation, notification, visibility, pos.z, pos.w, width.getValue(), height.getValue());

            updateColors();
            drawNotification(notification, animation, width.getValue(), height.getValue(), displayTime.getValue());
        }
    }

    private Vector4f calculatePosition(int screenWidth, int screenHeight, int index, float width, float height, float margin) {
        float targetX, targetY, directionX, directionY;

        float targetY1 = screenHeight - height - margin - index * (height + margin);
        switch (position.getMode()) {
            case "Top-Left":
                targetX = margin;
                targetY = margin + index * (height + margin);
                directionX = -1;
                directionY = 1;
                break;

            case "Top-Right":
                targetX = screenWidth - width - margin;
                targetY = margin + index * (height + margin);
                directionX = 1;
                directionY = 1;
                break;

            case "Bottom-Left":
                targetX = margin;
                targetY = targetY1;
                directionX = -1;
                directionY = -1;
                break;

            case "Bottom-Right":
            default:
                targetX = screenWidth - width - margin;
                targetY = targetY1;
                directionX = 1;
                directionY = -1;
                break;
        }

        return new Vector4f(targetX, targetY, directionX, directionY);
    }

    private Animation2D createAnimation(float targetX, float targetY, float directionX, float directionY, int screenWidth, int screenHeight, float width, float height) {
        float startX, startY;

        if (directionX < 0) startX = -width;
        else startX = screenWidth;

        if (directionY < 0) startY = screenHeight + height;
        else startY = -height;

        return new Animation2D(startX, startY, targetX, targetY);
    }

    private void applyAnimationStyle(Animation2D animation, Notification notification, float visibility, float directionX, float directionY, float width, float height) {
        long timeSinceShow = System.currentTimeMillis() - notification.getLastMS();

        if (timeSinceShow < 150) {
            float scale = timeSinceShow / 150f;
            animation.x = animation.endX + (width / 2f) * (1 - scale);
            animation.y = animation.endY + (height / 2f) * (1 - scale);
        } else {
            animation.x = animation.endX;
            animation.y = animation.endY;
        }

        if (visibility < 0.3f) {
            float exitProgress = (0.3f - visibility) / 0.3f;
            animation.x += directionX * width * exitProgress;
            animation.y += directionY * height * exitProgress;
        }

        animation.update(10);
    }

    private void updateColors() {
        bgColors = bgColor.isFade() ?
                ColorUtils.fadeColor(bgColor.getColor(), bgColor.getFadeColor(), bgColor.getSpeed())
                : bgColor.getColor();
        if (line.isToggled()) lineColors = lineColor.isFade() ?
                ColorUtils.fadeColor(lineColor.getColor(), lineColor.getFadeColor(), lineColor.getSpeed())
                : lineColor.getColor();
        textColors = textColor.isFade() ?
                ColorUtils.fadeColor(textColor.getColor(), textColor.getFadeColor(), textColor.getSpeed())
                : textColor.getColor();
        textDisableColors = textDisableColor.isFade() ?
                ColorUtils.fadeColor(textDisableColor.getColor(), textDisableColor.getFadeColor(), textDisableColor.getSpeed())
                : textDisableColor.getColor();
        textEnableColors = textEnableColor.isFade() ?
                ColorUtils.fadeColor(textEnableColor.getColor(), textEnableColor.getFadeColor(), textEnableColor.getSpeed())
                : textEnableColor.getColor();
    }

    private void drawNotification(Notification notification, Animation2D animation, float width, float height, int displayTime) {
        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(animation.x, animation.y, width, height, bgRadius.getValue(), bgColorShadow.getColor(), bgColorShadow.getFadeColor(), bgColorShadow.getSpeed()));
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), Color.WHITE));
        }

        RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), bgColors);

        String text = notification.getModuleName() + ":";
        String toggleText = notification.isToggle() ? " enabled" : " disabled";
        font.drawString(text, animation.x + 8, animation.y + 2 + height / 2f - font.FONT_HEIGHT / 2f,
                textColors);

        font.drawString(toggleText,animation.x + 8 + font.getStringWidth(text), animation.y + 2 + height / 2f - font.FONT_HEIGHT / 2f,
                (notification.isToggle() ? textEnableColors : textDisableColors));

        if (line.isToggled()) {
            float progress = (displayTime - (System.currentTimeMillis() - notification.getLastMS())) / (float)displayTime;

            StencilUtils.renderStencil(
                    () -> RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), bgColors),
                    () -> RoundedUtils.drawRect(animation.x, animation.y + height - lineSize.getValue(), width * progress, 1 + lineSize.getValue(), 1, lineColors)
            );
        }
    }

    private float calculateVisibility(Notification notification, int displayTime) {
        long timeSinceShow = System.currentTimeMillis() - notification.getLastMS();

        if (timeSinceShow < 200) {
            return timeSinceShow / 200f;
        }

        if (timeSinceShow > displayTime - 500) {
            return (displayTime - timeSinceShow) / 500f;
        }

        return 1f;
    }
}