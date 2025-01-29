package me.hackclient.utils.predict;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class SimulatedPlayer implements InstanceAccess {


    public static PlayerInfo getPredictedPos(float forward, float strafe, double motionX, double motionY, double motionZ, double posX, double posY, double posZ, final boolean isJumping) {
        strafe *= 0.98f;
        forward *= 0.98f;
        float f4 = 0.91f;
        final boolean isSprinting = mc.thePlayer.isSprinting();
        if (isJumping && mc.thePlayer.onGround && mc.thePlayer.jumpTicks == 0) {
            motionY = 0.42;
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                motionY += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f;
            }
            if (isSprinting) {
                final float f5 = mc.thePlayer.rotationYaw * 0.017453292f;
                motionX -= MathHelper.sin(f5) * 0.2f;
                motionZ += MathHelper.cos(f5) * 0.2f;
            }
        }
        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(posY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91f;
        }
        final float f6 = 0.16277136f / (f4 * f4 * f4);
        float friction;
        if (mc.thePlayer.onGround) {
            friction = mc.thePlayer.getAIMoveSpeed() * f6;
        }
        else {
            friction = mc.thePlayer.jumpMovementFactor;
        }
        float f7 = strafe * strafe + forward * forward;
        if (f7 >= 1.0E-4f) {
            f7 = MathHelper.sqrt_float(f7);
            if (f7 < 1.0f) {
                f7 = 1.0f;
            }
            f7 = friction / f7;
            strafe *= f7;
            forward *= f7;
            final float f8 = MathHelper.sin(mc.thePlayer.rotationYaw * 3.1415927f / 180.0f);
            final float f9 = MathHelper.cos(mc.thePlayer.rotationYaw * 3.1415927f / 180.0f);
            motionX += strafe * f9 - forward * f8;
            motionZ += forward * f9 + strafe * f8;
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        f4 = 0.91f;
        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91f;
        }
        if (mc.thePlayer.worldObj.isRemote && (!mc.thePlayer.worldObj.isBlockLoaded(new BlockPos((int)posX, 0, (int)posZ)) || !mc.thePlayer.worldObj.getChunkFromBlockCoords(new BlockPos((int)posX, 0, (int)posZ)).isLoaded())) {
            if (posY > 0.0) {
                motionY = -0.1;
            }
            else {
                motionY = 0.0;
            }
        }
        else {
            motionY -= 0.08;
        }
        motionY *= 0.9800000190734863;
        motionX *= f4;
        motionZ *= f4;
        return new PlayerInfo(posX, posY, posZ, motionX, motionY, motionZ);
    }
}


