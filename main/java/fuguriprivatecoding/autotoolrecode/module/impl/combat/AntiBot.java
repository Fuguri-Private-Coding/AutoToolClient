package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import net.minecraft.entity.Entity;
import java.util.Objects;

@ModuleInfo(name = "AntiBot", category = Category.COMBAT, description = "Убирает ботов.")
public class AntiBot extends Module {

    public static boolean isBot(Entity ent) {
        AntiBot antiBot = Modules.getModule(AntiBot.class);
        return antiBot.isToggled() && Objects.equals(ent.getCustomNameTag(), "");
    }
}
