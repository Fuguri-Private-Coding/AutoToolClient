package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class RenderItemEvent extends Event {
    float swingProgress, equipProgress;
}