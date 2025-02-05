package me.hackclient.module.impl.move;

import com.ibm.icu.text.UFormat;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.move.MoveUtils;

@ModuleInfo(name = "LongJump", category = Category.MOVE)
public class LongJump extends Module {

    ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "FunnyMcSkyPvp",
            new String[] {
                    "FunnyMcSkyPvp"
            }
    );

    @Override
    public void onEnable() {
        super.onEnable();
    }

    boolean flag;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            if (mc.thePlayer.onGround) {
                if (flag) {
                    mc.thePlayer.stopMotion();
                    flag = false;
                    toggle();
                    return;
                }
                mc.thePlayer.jump();
                return;
            }
            if (!flag) {
                MoveUtils.strafe(1f);
                flag = true;
            }
        }
    }
}
