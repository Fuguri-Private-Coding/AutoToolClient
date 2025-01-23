package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.shader.impl.TestBloomUtils;
import net.minecraft.client.gui.Gui;

@ModuleInfo(name = "Test", category = Category.MISC)
public class Test extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render2DEvent) {
            TestBloomUtils.add(() -> Gui.drawRect(50, 50, 150, 100, -1));
        }
    }
}