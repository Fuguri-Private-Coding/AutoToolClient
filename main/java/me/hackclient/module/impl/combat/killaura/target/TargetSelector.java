package me.hackclient.module.impl.combat.killaura.target;

import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TargetSelector implements InstanceAccess {

	public EntityPlayer selectPlayer(float distance) {

		if (mc.theWorld == null)
			return null;

		List<Entity> worldEntities = new ArrayList<>(mc.theWorld.loadedEntityList);
        worldEntities.remove(mc.thePlayer);

		Stream<Entity> entityStream = worldEntities.stream();
		entityStream = entityStream.filter(EntityPlayer.class::isInstance)
				.filter(entity -> DistanceUtils.getDistanceToEntity(entity) < distance)
				.filter(entity -> !((EntityPlayer) entity).isFriend())
				.sorted(Comparator.comparingDouble(
						RotationUtils::getFovToEntity)
				);

		return (EntityPlayer) entityStream.findFirst().orElse(null);
	}
}
