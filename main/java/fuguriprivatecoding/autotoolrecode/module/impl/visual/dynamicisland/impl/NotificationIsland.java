package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Notifications;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.TextureUtils;
import net.minecraft.util.ResourceLocation;

public class NotificationIsland extends IslandComponent {
    private final String text;
    private final boolean toggle;

    public NotificationIsland(String text, boolean toggle) {
        this.text = text;
        this.toggle = toggle;
    }

    ResourceLocation checkLocation = Client.of("image/check.png");
    ResourceLocation closeLocation = Client.of("image/close.png");

    public static IslandComponent create(IslandContext ctx) {
        if (!Modules.getModule(Notifications.class).isToggled() || Notifications.notifications.isEmpty()) {
            return null;
        }

        Notification notification = Notifications.notifications.getLast();
        String toggleText = notification.isToggled() ? "включен" : "выключен";

        return new NotificationIsland("Модуль " + notification.getText() + " был " + toggleText + ".", notification.isToggled());
    }

    @Override
    public String getKey(IslandContext ctx) {
        return text;
    }

    @Override
    public float getWidth(IslandContext ctx) {
        return ctx.regular().width(text, 8) + 10;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void draw(IslandContext ctx) {
        TextureUtils.texture(toggle ? checkLocation : closeLocation, -2.5f * ctx.textAlpha().getValue(), -2.5f, 10, 10, 5f, 1f, Colors.WHITE.withAlpha(ctx.textAlpha().getValue()));
        ctx.regular().draw(text, 10, 0, 8, ctx.whiteColor().withAlpha(ctx.textAlpha().getValue()));
    }
}
