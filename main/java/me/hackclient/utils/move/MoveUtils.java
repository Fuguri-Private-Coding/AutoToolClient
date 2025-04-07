package me.hackclient.utils.move;

import lombok.experimental.UtilityClass;
import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

@UtilityClass
public class MoveUtils implements InstanceAccess {

    public boolean canSprint() {
        return mc.thePlayer.moveForward >= 0.8F
                && !mc.thePlayer.isCollidedHorizontally
                && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying)
                && !mc.thePlayer.isPotionActive(Potion.blindness)
                && !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isSneaking();
    }

    public static void silentMoveFix(MoveFlyingEvent event) {
        int dif = (int) ((MathHelper.wrapDegree((mc.thePlayer.rotationYaw - Rotation.getServerRotation().getYaw() - 23.5f - 135)) + 180) / 45);
        float yaw = Rotation.getServerRotation().getYaw();
        float strafe = event.getStrafe();
        float forward = event.getForward();
        float friction = event.getFriction();
        float calcForward = 0f;
        float calcStrafe = 0f;

        switch (dif) {
            case 0: {
                calcForward = forward;
                calcStrafe = strafe;
            }
            break;

            case 1: {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
            }
            break;

            case 2: {
                calcForward = strafe;
                calcStrafe = -forward;
            }
            break;

            case 3: {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
            }
            break;

            case 4: {
                calcForward = -forward;
                calcStrafe = -strafe;
            }
            break;

            case 5: {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
            }
            break;

            case 6: {
                calcForward = -strafe;
                calcStrafe = forward;
            }
            break;

            case 7: {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
            }
            break;
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f;
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f;
        }

        float d = calcStrafe * calcStrafe + calcForward * calcForward;

        if (d >= 1.0E-4f) {
            d = MathHelper.sqrt_float(d);

            if (d < 1.0f) {
                d = 1.0f;
            }

            d = friction / d;
            calcStrafe = calcStrafe * d;
            calcForward = calcForward * d;
            float yawSin = MathHelper.sin((float) (yaw * Math.PI / 180f));
            float yawCos = MathHelper.cos((float) (yaw * Math.PI / 180f));
            mc.thePlayer.motionX += calcStrafe * yawCos - calcForward * yawSin;
            mc.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin;
        }
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

    public static double getDirection(float yaw) {
        float rotationYaw = yaw;

        if (mc.thePlayer.moveForward < 0.0F) {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;

        if (mc.thePlayer.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            rotationYaw -= 90.0F * forward;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            rotationYaw += 90.0F * forward;
        }

        return Math.toRadians(rotationYaw);
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
