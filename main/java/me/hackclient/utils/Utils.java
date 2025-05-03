package me.hackclient.utils;

import me.hackclient.utils.interfaces.Imports;
import net.minecraft.util.BlockPos;

public class Utils implements Imports {

    public static boolean isWorldLoaded() {
        return mc.thePlayer != null && mc.theWorld != null && mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ));
    }
}
