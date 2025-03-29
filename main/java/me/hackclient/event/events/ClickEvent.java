package me.hackclient.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.hackclient.event.Event;

@AllArgsConstructor
@Getter
public class ClickEvent extends Event {
    private final Button button;

    public enum Button {
        LEFT, RIGHT;
    }
}
