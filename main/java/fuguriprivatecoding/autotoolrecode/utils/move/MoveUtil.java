package fuguriprivatecoding.autotoolrecode.utils.move;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

public class MoveUtil implements Imports {

    public static double getSpeed() {
        assert mc.thePlayer != null;
        double motx = mc.thePlayer.motionX;
        double motz = mc.thePlayer.motionZ;
        return Math.sqrt(motx * motx + motz * motz);
    }

    public static double getXT() {
        return -Math.sin(MoveUtils.getDir());
    }

    public static double getZT() {
        return Math.cos(MoveUtils.getDir());
    }

    public static void strafe2(double motion) {
        assert mc.thePlayer != null;
        mc.thePlayer.motionX = getXT() * motion;
        mc.thePlayer.motionZ = getZT() * motion;
    }

    public static void limit2speed(double speedd) {
        assert mc.thePlayer != null;
        while(getSpeed() > speedd) {
            mc.thePlayer.motionX *= 0.999;
            mc.thePlayer.motionZ *= 0.999;
        }
    }

    public static void minstrafe(double motion) {
        if(getSpeed() < motion) {
            strafe2(motion);
        }
    }
}