package me.hackclient.utils.target;

import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TargetFinder implements InstanceAccess {

    public static EntityLivingBase findTarget(double distance, boolean players, boolean mobs, boolean animal) {
        List<Entity> copiedList = new CopyOnWriteArrayList<>(mc.theWorld.loadedEntityList);

        return (EntityLivingBase) copiedList.stream()
                .filter(entity -> entity != mc.thePlayer)
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(entity -> (entity instanceof EntityPlayer && players) || (entity instanceof EntityMob && mobs) || (entity instanceof EntityAnimal && animal))
                .filter(entity -> !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isFriend())
                .filter(entity -> DistanceUtils.getDistanceToEntity(entity) < distance)
                .min(Comparator.comparing(RotationUtils::getFovToEntity)).orElse(null);
    }

}
