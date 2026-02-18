package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "AntiBot", category = Category.COMBAT, description = "Убирает ботов.")
public class AntiBot extends Module {

    private static final List<EntityLivingBase> bots = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            bots.clear();
            if (mc.thePlayer.ticksExisted > 110) {
                for (EntityPlayer entity : mc.theWorld.playerEntities) {
                    if (entity != mc.thePlayer && isBot((Entity) entity) && !bots.contains(entity)) {
                        bots.add(entity);
                    }
                }
            }
        }
    }

    private boolean isBot(Entity ent) {
        return ent.getCustomNameTag() == "";
    }

    public static boolean isBot(EntityPlayer entity) {
        return bots.contains(entity);
    }
}
