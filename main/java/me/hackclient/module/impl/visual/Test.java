package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(
        name = "Test",
        category = Category.VISUAL
)
public class Test extends Module {


    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {

        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

}