package fuguriprivatecoding.autotoolrecode.utils.target;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TargetFinder implements Imports {

//    public static EntityLivingBase findTarget(double distance, boolean players, boolean mobs, boolean animal) {
//        List<Entity> copiedList = new CopyOnWriteArrayList<>(mc.theWorld.loadedEntityList);
//
//        return (EntityLivingBase) copiedList.stream()
//            .filter(entity -> entity != mc.thePlayer)
//            .filter(entity -> entity instanceof EntityLivingBase)
//            .filter(entity -> (entity instanceof EntityPlayer && players) || (entity instanceof EntityMob && mobs) || (entity instanceof EntityAnimal && animal))
//            .filter(entity -> players && entity instanceof EntityPlayer player && player.isValid())
//            .filter(entity -> DistanceUtils.getDistance(entity) < distance)
//            .min(Comparator.comparing(RotUtils::getFovToEntity)).orElse(null);
//    }

    public static List<EntityLivingBase> findTarget(double distance, boolean checkPlayers, boolean checkMobs, boolean checkAnimals, boolean checkVillager) {
        List<EntityLivingBase> entityList = new CopyOnWriteArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!isValidEntity(entity, distance))
                continue;

            if (entity instanceof EntityLivingBase target && matchesTargetType(target, checkPlayers, checkMobs, checkAnimals, checkVillager)) {
                entityList.add(target);
            }
        }

        return entityList;
    }

    private static boolean isValidEntity(Entity entity, double distance) {
        return entity != mc.thePlayer && entity.isEntityAlive() && DistanceUtils.getDistance(entity) <= distance;
    }

    private static boolean matchesTargetType(EntityLivingBase entity, boolean checkPlayers, boolean checkMobs, boolean checkAnimals, boolean checkVillager) {
        return switch (entity) {
            case EntityPlayer player -> checkPlayers && !player.isFriend() && !player.isBot() && !player.isTeam();
            case EntityMob ignore -> checkMobs;
            case EntityAnimal ignore -> checkAnimals;
            case EntityVillager ignore -> checkVillager;
            default -> false;
        };
    }

}
