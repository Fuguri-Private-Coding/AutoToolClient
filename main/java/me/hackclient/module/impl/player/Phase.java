package me.hackclient.module.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.hackclient.event.Event;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

@ModuleInfo(
        name = "Phase",
        category = Category.PLAYER
)
public class Phase extends Module {
    private final Map<BlockPos, IBlockState> originalBlocks = new HashMap<>();

    public void onEnable() {
        this.originalBlocks.clear();
    }

    public void onDisable() {
        for (Map.Entry<BlockPos, IBlockState> entry : this.originalBlocks.entrySet()) {
            mc.theWorld.setBlockState(entry.getKey(), entry.getValue());
        }

        this.originalBlocks.clear();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            EntityPlayerSP player = mc.thePlayer;
            BlockPos playerPos = player.getPosition();
            int playerY = playerPos.getY();

            int breakPhaseHeightOffset = 0;
            for (int y = playerY - 1 - breakPhaseHeightOffset; y <= playerY + 2 - breakPhaseHeightOffset; ++y) {
                for (int x = playerPos.getX() - 1; x <= playerPos.getX() + 1; ++x) {
                    for (int z = playerPos.getZ() - 1; z <= playerPos.getZ() + 1; ++z) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (y < playerY - 1 || y > playerY || y == playerY) {
                            IBlockState currentState = mc.theWorld.getBlockState(pos);
                            if (!currentState.getBlock().equals(Blocks.air)) {
                                if (!this.originalBlocks.containsKey(pos)) {
                                    this.originalBlocks.put(pos, currentState);
                                }

                                mc.theWorld.setBlockState(pos, Blocks.air.getDefaultState());
                            }
                        }
                    }
                }
            }

            List<BlockPos> blocksToRestore = getBlockPos(playerPos, playerY, breakPhaseHeightOffset);

            for (BlockPos pos : blocksToRestore) {
                IBlockState originalState = this.originalBlocks.remove(pos);
                mc.theWorld.setBlockState(pos, originalState);
            }
        }
    }

    private List<BlockPos> getBlockPos(BlockPos playerPos, int playerY, int breakPhaseHeightOffset) {
        List<BlockPos> blocksToRestore = new ArrayList<>();

        for (Map.Entry<BlockPos, IBlockState> entry : this.originalBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            if (Math.abs(pos.getX() - playerPos.getX()) > 1 || Math.abs(pos.getZ() - playerPos.getZ()) > 1 || pos.getY() < playerY - 1 - breakPhaseHeightOffset || pos.getY() > playerY + 2 - breakPhaseHeightOffset || (pos.getY() == playerY - 1 && pos.getX() == playerPos.getX() && pos.getZ() == playerPos.getZ())) {
                blocksToRestore.add(pos);
            }
        }
        return blocksToRestore;
    }
}
