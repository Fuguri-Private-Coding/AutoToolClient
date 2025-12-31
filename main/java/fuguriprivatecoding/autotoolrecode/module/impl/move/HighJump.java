package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.JumpEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;

@ModuleInfo(name = "HighJump", category = Category.MOVE, description = "Позволяет вам высоко прыгать.")
public class HighJump extends Module {

    Mode mode = new Mode("HighMode", this)
        .addModes("Motion", "Matrix")
        .setMode("Matrix")
        ;

    FloatSetting motion = new FloatSetting("Motion", this, () -> mode.is("Motion"), 0, 5, 0.5f, 0.01f);

    int ticksSinceJump;
    boolean active, falling, moving;

    @Override
    public void onEnable() {
        this.ticksSinceJump = 0;
        this.active = this.falling = false;
        this.moving = MoveUtils.isMoving();
        super.onEnable();
    }

    @Override
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Matrix" -> {
                if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE) {
                    e.setOnGround(false);
                }

                if (event instanceof UpdateEvent) {
                    if (!this.moving) {
                        MoveUtils.strafe(0.16, 1);
                        this.moving = true;
                    }

                    if (mc.thePlayer.isCollidedVertically) {
                        this.active = true;
                    }

                    if (this.ticksSinceJump == 1) {
                        mc.thePlayer.onGround = false;
                        mc.thePlayer.motionY = 0.998;
                    }

                    if (mc.thePlayer.isCollidedVertically && this.ticksSinceJump > 4) {
                        this.toggle();
                    }

                    if (!mc.thePlayer.onGround && this.ticksSinceJump >= 2) {
                        mc.thePlayer.motionY += 0.0034999;
                        if (!this.falling && mc.thePlayer.motionY < (double) 0.0F && mc.thePlayer.motionY > -0.05) {
                            mc.thePlayer.motionY = 0.0029999;
                            this.falling = true;
                            this.toggle();
                        }
                    }

                    if (this.active) {
                        ++this.ticksSinceJump;
                    }
                }
            }

            case "Motion" -> {
                if (event instanceof JumpEvent e) {
                    e.setHeight(motion.getValue());
                }
            }
        }
    }
}
