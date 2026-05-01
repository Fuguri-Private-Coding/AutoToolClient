package fuguriprivatecoding.autotoolrecode.utils.target;

import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TargetFinder implements Imports {

    public static EntityLivingBase findTarget(double distance, boolean players, boolean mobs, boolean animal) {
        List<Entity> copiedList = new CopyOnWriteArrayList<>(mc.theWorld.loadedEntityList);

        return (EntityLivingBase) copiedList.stream()
            .filter(entity -> entity != mc.thePlayer)
            .filter(entity -> entity instanceof EntityLivingBase)
            .filter(entity -> (entity instanceof EntityPlayer && players) || (entity instanceof EntityMob && mobs) || (entity instanceof EntityAnimal && animal))
            .filter(entity -> players && entity instanceof EntityPlayer player && player.isValid())
            .filter(entity -> DistanceUtils.getDistance(entity) < distance)
            .min(Comparator.comparing(RotUtils::getFovToEntity)).orElse(null);

    }

}
