package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.ClickEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@ModuleInfo(
        name = "CPSCounter",
        category = Category.VISUAL
)
public class CPSCounter extends Module {
    
    private final List<Long> leftClicks = new CopyOnWriteArrayList<>();
    private final List<Long> rightClicks = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof ClickEvent clickEvent) {
            switch (clickEvent.getButton()) {
                case LEFT -> leftClicks.add(System.currentTimeMillis());
                case RIGHT -> rightClicks.add(System.currentTimeMillis());
            }
        }

        if (event instanceof Render2DEvent) {
            leftClicks.removeIf(time -> System.currentTimeMillis() - time >= 1000L);
            rightClicks.removeIf(time -> System.currentTimeMillis() - time >= 1000L);

            int leftCps = leftClicks.size();
            int rightCps = rightClicks.size();

            mc.fontRendererObj.drawString(leftCps + " / " + rightCps, 300, 300, -1, true);
        }
    }
}
 