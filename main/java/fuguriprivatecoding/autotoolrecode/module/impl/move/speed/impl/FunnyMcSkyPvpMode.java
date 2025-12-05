package fuguriprivatecoding.autotoolrecode.module.impl.move.speed.impl;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.AbstractSpeedMode;

public class FunnyMcSkyPvpMode extends AbstractSpeedMode {
    
    private int ticks = 0;
    
    public FunnyMcSkyPvpMode() {
        super("FunnyMcSkyPvp");
    }
    
    @Override
    public void onDisable(Speed speed) {
        ticks = 0;
    }
    
    @Override
    public void handleEvent(Event event, Speed speed) {
        if (event instanceof UpdateEvent) {
            if (mc.thePlayer.onGround) {
                ticks = 0;
                mc.thePlayer.jump();
                mc.thePlayer.motionY = 0.4D;
            } else {
                if (ticks < 10) {
                    double[] motions = new double[]{0, 0, 0, 0, 0.1912, 0.3, 1, 0, 0, 0, 0};
                    double motion = motions[ticks++];
                    mc.thePlayer.motionY -= motion;
                }
                if (mc.thePlayer.isBurning()) {
                    mc.thePlayer.motionY = -1;
                }
            }
        }
    }
}