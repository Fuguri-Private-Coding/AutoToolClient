package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Notifications;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;

public class NotificationIsland extends IslandComponent {
    private final String text;

    public NotificationIsland(String text) {
        this.text = text;
    }

    public static IslandComponent create(IslandContext ctx) {
        if (!Modules.getModule(Notifications.class).isToggled() || Notifications.notifications.isEmpty()) {
            return null;
        }

        Notification notification = Notifications.notifications.getLast();
        String toggleText = notification.isToggled() ? "включен" : "выключен";

        return new NotificationIsland("Модуль " + notification.getText() + " был " + toggleText + ".");
    }

    @Override
    public String getKey(IslandContext ctx) {
        return text;
    }

    @Override
    public float getWidth(IslandContext ctx) {
        return ctx.regular.width(text, 8);
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void draw(IslandContext ctx) {
        ctx.regular.draw(text, 0, 0, 8, ctx.whiteColor.withAlpha(ctx.textAlpha.getValue()));
    }
}
