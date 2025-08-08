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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    CheckBox textFade = new CheckBox("Text Fade", this, false);
    ColorSetting textColor1 = new ColorSetting("Text Color1", this, 1,1,1,1);
    ColorSetting textColor2 = new ColorSetting("Text Color2", this,() -> textFade.isToggled(), 0,0,0,1);
    FloatSetting textSpeed = new FloatSetting("Text Speed", this,() -> textFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox textDisableFade = new CheckBox("Text Disable Fade", this, false);
    ColorSetting textDisableColor1 = new ColorSetting("Text Disable Color1", this, 1,1,1,1);
    ColorSetting textDisableColor2 = new ColorSetting("Text Disable Color2", this,() -> textDisableFade.isToggled(), 0,0,0,1);
    FloatSetting textDisableSpeed = new FloatSetting("Text Disable Speed", this,() -> textDisableFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox textEnableFade = new CheckBox("Text Enable Fade", this, false);
    ColorSetting textEnableColor1 = new ColorSetting("Text Enable Color1", this, 1,1,1,1);
    ColorSetting textEnableColor2 = new ColorSetting("Text Enable Color2", this,() -> textEnableFade.isToggled(), 0,0,0,1);
    FloatSetting textEnableSpeed = new FloatSetting("Text Enable Speed", this,() -> textEnableFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox bgFade = new CheckBox("Background Fade", this, false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> bgFade.isToggled(),0.1f, 20, 1, 0.1f);
    IntegerSetting bgRadius = new IntegerSetting("Background Radius", this, 1,50,10);

    CheckBox line = new CheckBox("Line",this, true);
    FloatSetting lineSize = new FloatSetting("Line Size", this,() -> line.isToggled(),0f, 10, 1, 0.1f);
    CheckBox lineFade = new CheckBox("Line Fade", this, () -> line.isToggled(), false);
    ColorSetting lineColor1 = new ColorSetting("Line Color1", this,() -> line.isToggled(), 0,0,0,1);
    ColorSetting lineColor2 = new ColorSetting("Line Color2", this,() -> line.isToggled() && lineFade.isToggled(), 0,0,0,1);
    FloatSetting lineSpeed = new FloatSetting("Line Speed", this,() -> line.isToggled() && lineFade.isToggled(),0.1f, 20, 1, 0.1f);

    ClientFontRenderer font = Client.INST.getFonts().fonts.get("JetBrains");

    public static Notifications instance;
    private final List<Notification> notificationList = new CopyOnWriteArrayList<>();
    private final StopWatch timer = new StopWatch();

    Color bgColor, lineColor, textColor, textDisableColor, textEnableColor;

    private Glow glow;
    private Blur blur;

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
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        if (glow == null) glow = Client.INST.getModuleManager().getModule(Glow.class);
        if (event instanceof Render2DEvent e) {
            renderNotifications(e.getWidth(), e.getHeight());
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
        bgColor = bgFade.isToggled() ?
                ColorUtils.fadeColor(bgColor1.getColor(), bgColor2.getColor(), bgSpeed.getValue())
                : bgColor1.getColor();
        if (line.isToggled()) lineColor = lineFade.isToggled() ?
                ColorUtils.fadeColor(lineColor1.getColor(), lineColor2.getColor(), lineSpeed.getValue())
                : lineColor1.getColor();
        textColor = textFade.isToggled() ?
                ColorUtils.fadeColor(textColor1.getColor(), textColor2.getColor(), textSpeed.getValue())
                : textColor1.getColor();
        textDisableColor = textDisableFade.isToggled() ?
                ColorUtils.fadeColor(textDisableColor1.getColor(), textDisableColor2.getColor(), textDisableSpeed.getValue())
                : textDisableColor1.getColor();
        textEnableColor = textEnableFade.isToggled() ?
                ColorUtils.fadeColor(textEnableColor1.getColor(), textEnableColor2.getColor(), textEnableSpeed.getValue())
                : textEnableColor1.getColor();
    }

    private void drawNotification(Notification notification, Animation2D animation, float width, float height, int displayTime) {
        if (glow.isToggled() && glow.module.get("Notifications")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), Color.WHITE));
        }

        if (blur.isToggled() && blur.module.get("Notifications")) {
            GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), Color.WHITE));
        }

        RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), bgColor);

        String text = notification.getModuleName() + ":";
        String toggleText = notification.isToggle() ? " enabled" : " disabled";
        font.drawString(text, animation.x + 8, animation.y + 2 + height / 2f - font.FONT_HEIGHT / 2f,
                textColor);

        font.drawString(toggleText,animation.x + 8 + font.getStringWidth(text), animation.y + 2 + height / 2f - font.FONT_HEIGHT / 2f,
                (notification.isToggle() ? textEnableColor : textDisableColor));

        if (line.isToggled()) {
            float progress = (displayTime - (System.currentTimeMillis() - notification.getLastMS())) / (float)displayTime;

            StencilUtils.renderStencil(
                    () -> RoundedUtils.drawRect(animation.x, animation.y, width, height, bgRadius.getValue(), bgColor),
                    () -> RoundedUtils.drawRect(animation.x, animation.y + height - lineSize.getValue(), width * progress, 1 + lineSize.getValue(), 1, lineColor)
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