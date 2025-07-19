package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.world.WorldSettings;

@ModuleInfo(name = "FakeGameMode", category = Category.MISC)
public class FakeGameMode extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Survival", "Creative", "Spectator", "Adventure")
            ;

    WorldSettings.GameType lastGameType;

    @Override
    public void onDisable() {
        setGameMode(lastGameType);
        lastGameType = null;
    }

    @Override
    public void onEnable() {
        lastGameType = mc.playerController.getCurrentGameType();
        setGameMode(
            switch (mode.getMode()) {
                case "Creative" -> WorldSettings.GameType.CREATIVE;
                case "Spectator" -> WorldSettings.GameType.SPECTATOR;
                case "Adventure" -> WorldSettings.GameType.ADVENTURE;
                default -> WorldSettings.GameType.SURVIVAL;
            }
        );
    }

    private void setGameMode(WorldSettings.GameType gameType) {
        if (mc.thePlayer != null && mc.playerController != null && gameType != null) {
            mc.playerController.setGameType(gameType);
        }
    }
}
