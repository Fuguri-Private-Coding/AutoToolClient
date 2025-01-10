package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class ChangeHeadRotationEvent extends Event {
	float yaw, pitch;
}
