package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.Event;
import net.minecraft.entity.Entity;

@Setter
@Getter
@AllArgsConstructor
public class EntityKilledEvent extends Event {
    Entity entity;
}
