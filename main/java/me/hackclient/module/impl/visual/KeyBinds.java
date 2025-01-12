package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.shader.impl.BloomUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "KeyBinds", category = Category.VISUAL)
public class KeyBinds extends Module {

    @Override
    public void onEvent(Event event) {

        Bloom bloomModule = mm.getModule(Bloom.class);

        super.onEvent(event);
        if (event instanceof Render2DEvent) {
            if (bloomModule.isToggled() && bloomModule.keyBinds.isToggled()) {
                List<Runnable> list = new ArrayList<>();
                //list.add(() -> );
                //BloomUtils.drawBloom(list);
            }
        }
        drawMain();
    }

    private void drawMain() {

    }
}
