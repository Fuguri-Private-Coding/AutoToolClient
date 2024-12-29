package me.hackclient.utils.target;

import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetUtils implements InstanceAccess {
	public static EntityLivingBase[] findTargets(double range, SearchPriority searchPriority) {
		List<Entity> loadedEntityList = mc.theWorld.getLoadedEntityList();

		Stream<Entity> stream = loadedEntityList.stream().filter(entity -> entity instanceof EntityLivingBase && !(entity instanceof EntityPlayerSP));

		switch (searchPriority) {
			case DISTANCE: {
				stream = stream.sorted(Comparator.comparingDouble(DistanceUtils::getDistanceToEntity));
				break;
			}
			case FOV: {
				stream = stream.sorted(Comparator.comparingDouble(RotationUtils::getFovToEntity));
				break;
			}

		}

		return (EntityLivingBase[]) stream.toArray();
	}

	public static EntityLivingBase getTarget(double range, SearchPriority searchPriority) {
	/*	List<Entity> targets = mc.theWorld.getLoadedEntityList().stream().filter(EntityLivingBase.class::isInstance).toList();
		targets = targets.stream().filter(entity -> DistanceUtils.getDistanceToEntity(entity) <= range && entity != mc.thePlayer && !(entity instanceof EntityArmorStand)).toList();

		targets.sort(Comparator.comparingDouble(entity -> DistanceUtils.getDistanceToEntity(entity)));

		targets = targets.stream().filter(EntityPlayer.class::isInstance).collect(Collectors.toList());

		if (!targets.isEmpty()) {
			return  (EntityLivingBase) targets.get(0);
		}
		return null;*/
		List<Entity> targets = new ArrayList<>(mc.theWorld.loadedEntityList);

		targets = targets.stream().filter(entity -> entity.getDistanceToEntity(mc.thePlayer) < range && entity != mc.thePlayer).collect(Collectors.toList());
		targets.sort(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)));

		targets = targets.stream().filter(EntityPlayer.class::isInstance).collect(Collectors.toList());

		return (EntityLivingBase) targets.get(0);
	}
}
