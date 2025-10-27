package fuguriprivatecoding.autotoolrecode.utils.move;

import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotoolrecode.event.events.MoveEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
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

    private static float yaw = 0;

    public static boolean isMoveDiagonally(float yaw) {
        return Math.round(yaw / 45f) % 2 != 0;
    }

    public static float getDir() {

        float rotationYaw = mc.thePlayer.rotationYaw;

        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean backward = mc.gameSettings.keyBindBack.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();

        if (forward && !backward && !left && !right) {
            rotationYaw += 0.0f;
        } else if (backward && !forward && !left && !right) {
            rotationYaw += 180.0f;
        } else if (left && !right && !forward && !backward) {
            rotationYaw -= 90.0f;
        } else if (right && !left && !forward && !backward) {
            rotationYaw += 90.0f;
        } else if (forward && left && !backward && !right) {
            rotationYaw -= 45.0f;
        } else if (forward && right && !backward && !left) {
            rotationYaw += 45.0f;
        } else if (backward && left && !forward && !right) {
            rotationYaw -= 135.0f;
        } else if (backward && right && !forward && !left) {
            rotationYaw += 135.0f;
        }

        rotationYaw = rotationYaw % 360.0f;
        if (rotationYaw < 0) {
            rotationYaw += 360.0f;
        }

        return (float) rotationYaw;
    }

    public static float getDirs() {
        if (mc.gameSettings.keyBindForward.isKeyDown() && mc.gameSettings.keyBindLeft.isKeyDown()) {
            yaw = 45f;
        } else if (mc.gameSettings.keyBindForward.isKeyDown() && mc.gameSettings.keyBindRight.isKeyDown()) {
            yaw = -45f;
        } else if (mc.gameSettings.keyBindBack.isKeyDown() && mc.gameSettings.keyBindLeft.isKeyDown()) {
            yaw = 135f;
        } else if (mc.gameSettings.keyBindBack.isKeyDown() && mc.gameSettings.keyBindRight.isKeyDown()) {
            yaw = -135f;
        } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
            yaw = 180f;
        } else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            yaw = 90f;
        } else if (mc.gameSettings.keyBindRight.isKeyDown()) {
            yaw = -90f;
        } else if (mc.gameSettings.keyBindForward.isKeyDown()) {
            yaw = 0f;
        }

        return yaw;
    }

    public static float getYawFromKeybind() {
        return mc.thePlayer.rotationYaw - getDirs();
    }

    public static boolean isMovingStraight() {
        float direction = getYawFromKeybind() + 180;
        float movingYaw = Math.round(direction / 45) * 45;
        return movingYaw % 90 == 0f;
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

    public void moveFix(MoveEvent e, float targetYaw) {
        if (e.getForward() == 0f && e.getStrafe() == 0f)
            return;

        float closestDiff = Float.MAX_VALUE;

        for (float forward = -1; forward <= 1f; forward++) {
            for (float strafe = -1; strafe <= 1f; strafe++) {
                if (forward == 0f && strafe == 0)
                    continue;

                float diff = Math.abs(MathHelper.wrapDegree(targetYaw - getDirection(Rot.getServerRotation().getYaw(), forward, strafe)));
                if (diff < closestDiff) {
                    closestDiff = diff;
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

    public BlockPos getDirectionalBlockPos(float edgeOffset, float yOffset) {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY - yOffset;
        double z = mc.thePlayer.posZ;

        boolean movingX = Math.abs(mc.thePlayer.motionX) > 0.1;
        boolean movingZ = Math.abs(mc.thePlayer.motionZ) > 0.1;

        if (movingX || movingZ) {
            if (Math.abs(mc.thePlayer.motionX) > Math.abs(mc.thePlayer.motionZ)) {
                x += (mc.thePlayer.motionX > 0) ? -edgeOffset : edgeOffset;
            } else {
                z += (mc.thePlayer.motionZ > 0) ? -edgeOffset : edgeOffset;
            }
        }

        return new BlockPos(x, y, z);
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


    public static double getSpeedNew() {
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

    public static void stopMotion() {
        mc.thePlayer.motionX = 0.0F;
        mc.thePlayer.motionZ = 0.0F;
    }

    public static void keyBindStop() {
        mc.gameSettings.keyBindForward.pressed = false;
        mc.gameSettings.keyBindBack.pressed = false;
        mc.gameSettings.keyBindLeft.pressed = false;
        mc.gameSettings.keyBindRight.pressed = false;
    }
}
