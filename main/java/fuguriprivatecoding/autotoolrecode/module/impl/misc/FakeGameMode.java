package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import net.minecraft.world.WorldSettings;

@ModuleInfo(name = "FakeGameMode", category = Category.MISC, description = "Изменяет вам режим игры.")
public class FakeGameMode extends Module {

    Mode mode = new Mode("Mode", this)
        .addModes("Survival", "Creative", "Spectator", "Adventure")
        .setMode("Creative")
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
        setGameMode(getGameType(mode.getMode()));
    }

    @Override
    public void onEvent(Event event) {
        WorldSettings.GameType newGameType = getGameType(mode.getMode());
        if (mc.playerController.getCurrentGameType() != newGameType) {
            if (event instanceof TickEvent) {
                setGameMode(newGameType);
            }
        }
    }

    private WorldSettings.GameType getGameType(String mode) {
        return switch (mode) {
            case "Creative" -> WorldSettings.GameType.CREATIVE;
            case "Spectator" -> WorldSettings.GameType.SPECTATOR;
            case "Adventure" -> WorldSettings.GameType.ADVENTURE;
            default -> WorldSettings.GameType.SURVIVAL;
        };
    }

    private void setGameMode(WorldSettings.GameType gameType) {
        if (mc.thePlayer != null && mc.playerController != null && gameType != null) {
            mc.playerController.setGameType(gameType);
        }
    }
}
