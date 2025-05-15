package fuguriprivatecoding.autotool.utils.move;

import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotool.event.events.MoveEvent;
import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

@UtilityClass
public class MoveUtils implements Imports {

    public boolean canSprint() {
        return mc.thePlayer.moveForward > 0.8F
                && !mc.thePlayer.isCollidedHorizontally
                && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying)
                && !mc.thePlayer.isPotionActive(Potion.blindness)
                && !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isSneaking();
    }

    public static boolean isMoving() {
        return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F && !mc.thePlayer.isCollidedHorizontally;
    }

    public static void setSpeed(float f2) {
        mc.thePlayer.motionX = -(Math.sin(direction()) * (double)f2);
        mc.thePlayer.motionZ = Math.cos(direction()) * (double)f2;
    }

    public static void setSpeed(float f2, boolean strafe) {
        double d = Math.toRadians(getYaw(strafe));
        mc.thePlayer.motionX = -(Math.sin(d) * (double)f2);
        mc.thePlayer.motionZ = Math.cos(d) * (double)f2;
    }

    public static void strafe(double d) {
        if (isMoving()) {
            double direction = direction();
            mc.thePlayer.motionX = -Math.sin(direction) * d;
            mc.thePlayer.motionZ = Math.cos(direction) * d;
        }
    }

    public static float getYaw(boolean strafe) {
        return strafe ? (float) Math.toDegrees(direction()) : mc.thePlayer.rotationYaw;
    }

    public static double direction() {
        float f = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0F) {
            f += 180.0F;
        }

        float f2 = 1.0F;
        if (mc.thePlayer.moveForward < 0.0F) {
            f2 = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            f2 = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            f -= 90.0F * f2;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            f += 90.0F * f2;
        }

        return Math.toRadians(f);
    }

    public static void moveFix(MoveEvent e, float targetYaw) {
        if (e.getForward() == 0 && e.getStrafe() == 0) {
            return;
        }

        //getDirection(mc.thePlayer.rotationYaw, eventForward, eventStrafe);
        float closestDiff = Float.MAX_VALUE;

        for (float forward = -1f; forward <= 1f; forward++) {
            for (float strafe = -1f; strafe <= 1f; strafe++) {
                if (forward == 0f && strafe == 0f) {
                    continue;
                }

                float direction = getDirection(mc.thePlayer.lastReportedYaw, forward, strafe);
                float difference = Math.abs(MathHelper.wrapDegree(targetYaw - direction));

                if (difference < closestDiff) {
                    closestDiff = difference;
                    e.setForward(forward);
                    e.setStrafe(strafe);
                }
            }
        }
    }

    public static float getDirection(float yaw, float forward, float strafe) {
        if (forward < 0) {
            yaw += 180;
        }

        float forwardMult = 1f;

        if (forward < 0) forwardMult = -0.5f;
        else if (forward > 0) forwardMult = 0.5f;

        if (strafe > 0) yaw -= 90 * forwardMult;
        if (strafe < 0) yaw += 90 * forwardMult;

        return MathHelper.wrapDegree(yaw);
    }

    public static float getDirection(float yaw) {
        return getDirection(yaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing);
    }

    public static float getDirection() {
        return getDirection(mc.thePlayer.rotationYaw, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing);
    }

    public static float getSpeed() {
        double powX = Math.pow(mc.thePlayer.motionX, 2);
        double powY = Math.pow(mc.thePlayer.motionY, 2);
        double powZ = Math.pow(mc.thePlayer.motionZ, 2);
        return (float) Math.sqrt(powX + powY + powZ);
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void strafe(float speed) {
        strafe((float) MathHelper.wrapDegree(Math.toDegrees(getDirection(mc.thePlayer.rotationYaw))), speed);
    }

    public static void strafe(float yaw, float speed) {
        double yawRad = Math.toRadians(yaw);
        double yawSin = Math.sin(yawRad);
        double yawCos = Math.cos(yawRad);
        mc.thePlayer.motionX = -yawSin * speed;
        mc.thePlayer.motionZ = yawCos * speed;
    }

    public static void updateControls() {
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
        mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight);
        mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump);
        mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint);
    }
}
