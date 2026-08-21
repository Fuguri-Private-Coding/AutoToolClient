package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

@ModuleInfo(name = "Teams", description = "Добавляет тиммейтов в друзья.", category = Category.COMBAT)
public class Teams extends Module {

    Mode teamMode = new Mode("TeamMode", this)
        .addModes("Color", "Name")
        .setMode("Color")
        ;

    public static boolean isTeammate(EntityPlayer entity) {
        Teams teams = Modules.getModule(Teams.class);

        if (!teams.isToggled())
            return false;

        ScorePlayerTeam entityTeam = mc.theWorld.getScoreboard().getPlayersTeam(entity.getName());
        ScorePlayerTeam myTeam = mc.theWorld.getScoreboard().getPlayersTeam(mc.thePlayer.getName());

        if (entityTeam == null || myTeam == null) {
            return false;
        }

        return (teams.teamMode.is("Color") && entityTeam.getColorPrefix().equals(myTeam.getColorPrefix()))
            || (teams.teamMode.is("Name") && entityTeam.getTeamName().equals(myTeam.getTeamName()));
    }
}
