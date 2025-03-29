package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.CancelableEvent;
import net.minecraft.entity.Entity;

@Setter
@Getter
@AllArgsConstructor
public class AttackEvent extends CancelableEvent {
	final Entity hittingEntity;
	private boolean cancelSprint;
}
