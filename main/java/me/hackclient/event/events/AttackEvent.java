package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.hackclient.event.CancelableEvent;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public class AttackEvent extends CancelableEvent {
	final Entity hittingEntity;
}
