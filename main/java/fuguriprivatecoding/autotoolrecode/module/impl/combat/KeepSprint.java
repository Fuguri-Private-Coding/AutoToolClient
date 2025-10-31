package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.HitSlowDownEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "KeepSprint", category = Category.COMBAT, description = "Не замедлятся от удара.")
public class KeepSprint extends Module {
    
    private final FloatSetting hurtHitMotion = new FloatSetting("HurtHitMotion",this, 0f,1f,0.6f,0.1f);
    private final FloatSetting hitMotion = new FloatSetting("HitMotion",this, 0f,1f,0.6f,0.1f);

    private final CheckBox sprintSlowDownVelocity = new CheckBox("HurtHitSprint", this);
    private final CheckBox sprintSlowDownNormal = new CheckBox("HitSprint", this);
    private final CheckBox onlyInAir = new CheckBox("OnlyInAir", this);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof HitSlowDownEvent e) {
            if (mc.thePlayer.onGround && onlyInAir.isToggled()) return;

            if (mc.thePlayer.hurtTime > 0) {
                e.setSlowDown(hurtHitMotion.getValue());
                e.setSprint(sprintSlowDownVelocity.isToggled());
            } else {
                e.setSlowDown(hitMotion.getValue());
                e.setSprint(sprintSlowDownNormal.isToggled());
            }
        }
    }
}