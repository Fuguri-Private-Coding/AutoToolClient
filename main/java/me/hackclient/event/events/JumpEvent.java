package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.CancelableEvent;

@Setter
@Getter
@AllArgsConstructor
public class JumpEvent extends CancelableEvent {
	float yaw, height;
}
