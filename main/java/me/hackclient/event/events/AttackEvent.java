package me.hackclient.event.events;

import me.hackclient.event.CancelableEvent;
import net.minecraft.entity.Entity;

public class AttackEvent extends CancelableEvent {
	private final Entity hittingEntity;

	public AttackEvent(Entity hittingEntity) {
		this.hittingEntity = hittingEntity;
	}

	public Entity getHittingEntity() {
		return hittingEntity;
	}
}
