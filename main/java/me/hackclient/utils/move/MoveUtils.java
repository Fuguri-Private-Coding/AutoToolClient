package me.hackclient.utils.move;

import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.util.MathHelper;

public class MoveUtils implements InstanceAccess {

    public static void silentMoveFix(MoveFlyingEvent event)
    {
        int dif = (int)((MathHelper.wrapDegree((mc.thePlayer.rotationYaw - Rotation.getServerRotation().getYaw() - 23.5f - 135)) + 180) / 45);
        float yaw = Rotation.getServerRotation().getYaw();
        float strafe =event.getStrafe();//event.getStrafe()
        float forward = event.getForward();//event.getForward()
        float friction = event.getFriction();//event.getFriction()
        float calcForward = 0f;
        float calcStrafe = 0f;

        switch (dif)
        {
            case 0:
            {
                calcForward = forward;
                calcStrafe = strafe;
            }
            break;

            case 1:
            {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
            }
            break;

            case 2:
            {
                calcForward = strafe;
                calcStrafe = -forward;
            }
            break;

            case 3:
            {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
            }
            break;

            case 4:
            {
                calcForward = -forward;
                calcStrafe = -strafe;
            }
            break;

            case 5:
            {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
            }
            break;

            case 6:
            {
                calcForward = -strafe;
                calcStrafe = forward;
            }
            break;

            case 7:
            {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
            }
            break;
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f)
        {
            calcForward *= 0.5f;
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f)
        {
            calcStrafe *= 0.5f;
        }

        float d = calcStrafe * calcStrafe + calcForward * calcForward;

        if (d >= 1.0E-4f)
        {
            d = MathHelper.sqrt_float(d);

            if (d < 1.0f)
            {
                d = 1.0f;
            }

            d = friction / d;
            calcStrafe = calcStrafe * d;
            calcForward = calcForward * d;
            float yawSin = MathHelper.sin((float)(yaw * Math.PI / 180f));
            float yawCos = MathHelper.cos((float)(yaw * Math.PI / 180f));
            mc.thePlayer.motionX += calcStrafe * yawCos - calcForward * yawSin;
            mc.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin;
        }
    }

    public static boolean isMoving() {
        return mc.thePlayer.moveForward != 0;
    }

    public static double getDirection(float yaw)
    {
        float rotationYaw = yaw;

        if (mc.thePlayer.moveForward < 0.0F)
        {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;

        if (mc.thePlayer.moveForward < 0.0F)
        {
            forward = -0.5F;
        }
        else if (mc.thePlayer.moveForward > 0.0F)
        {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F)
        {
            rotationYaw -= 90.0F * forward;
        }

        if (mc.thePlayer.moveStrafing < 0.0F)
        {
            rotationYaw += 90.0F * forward;
        }

        return Math.toRadians(rotationYaw);
    }
}
