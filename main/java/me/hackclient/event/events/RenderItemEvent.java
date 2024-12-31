package me.hackclient.event.events;

import me.hackclient.event.Event;

public class RenderItemEvent extends Event {
    float swingProgress, equipProgress;

    public RenderItemEvent(float swingProgress, float equipProgress) {
        this.swingProgress = swingProgress;
        this.equipProgress = equipProgress;
    }

    public float getSwingProgress() {
        return swingProgress;
    }

    public void setSwingProgress(float swingProgress) {
        this.swingProgress = swingProgress;
    }

    public float getEquipProgress() {
        return equipProgress;
    }

    public void setEquipProgress(float equipProgress) {
        this.equipProgress = equipProgress;
    }
}