package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Teams", description = "Добавляет тиммейтов в друзья.", category = Category.COMBAT)
public class Teams extends Module {

    Mode teamMode = new Mode("TeamMode", this)
        .addModes("Color", "Name")
        .setMode("Color")
        ;

    @Getter
    List<EntityPlayer> teamList = new CopyOnWriteArrayList<>();

    @Override
    public void onDisable() {
        if (!teamList.isEmpty()) teamList.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Entity entity : mc.theWorld.playerEntities) {
                if (isTeammate(entity) && entity instanceof EntityPlayer entityPlayer) {
                    teamList.add(entityPlayer);
                }
            }
        }

        if (event instanceof WorldChangeEvent && !teamList.isEmpty()) teamList.clear();
    }

    private boolean isTeammate(Entity entity) {
        ScorePlayerTeam entityTeam = mc.theWorld.getScoreboard().getPlayersTeam(entity.getName());
        ScorePlayerTeam myTeam = mc.theWorld.getScoreboard().getPlayersTeam(mc.thePlayer.getName());
        return entity instanceof EntityPlayer && (entityTeam != null && myTeam != null &&
            entityTeam.getColorPrefix().equals(myTeam.getColorPrefix()) && teamMode.is("Color") ||
            entityTeam != null && myTeam != null && entityTeam.getTeamName().equals(myTeam.getTeamName()) && teamMode.is("Name"));
    }
}
