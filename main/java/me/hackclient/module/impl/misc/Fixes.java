package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.utils.move.MoveUtils;

@ModuleInfo(
        name = "Fixes",
        category = Category.MISC,
        toggled = true
)
public class Fixes extends Module {

    BooleanSetting clickFix = new BooleanSetting("ClickFix", this, true);
    BooleanSetting saveMoveKeys = new BooleanSetting("SaveMoveKeys", this, true);

    boolean prevGui = false;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MotionEvent) {
            if (mc.thePlayer != null && mc.theWorld != null && clickFix.isToggled()) {
                mc.leftClickCounter = 0;
            }

            if (mc.currentScreen == null && saveMoveKeys.isToggled()) {
                if (prevGui) MoveUtils.updateControls();
                prevGui = false;
            } else {
                prevGui = true;
            }
        }
    }
}
