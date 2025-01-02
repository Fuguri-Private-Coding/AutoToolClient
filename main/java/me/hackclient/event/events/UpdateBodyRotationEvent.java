package me.hackclient.event.events;

import me.hackclient.event.Event;

public class UpdateBodyRotationEvent extends Event {
    float yaw;

    public UpdateBodyRotationEvent(float yaw) {
        this.yaw = yaw;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
