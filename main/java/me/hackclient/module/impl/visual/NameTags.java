package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, toggled = true)
public class NameTags extends Module {

    public final FloatSetting scale = new FloatSetting("Scale", this, 0.1f, 3f, 1.6f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {

        }
    }
}
