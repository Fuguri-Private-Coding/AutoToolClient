package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import lombok.Getter;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;
import java.util.ArrayList;

@ModuleInfo(name = "RawMouseInput", category = Category.MISC, description = "Делает так чтобы движения мыши брались напрямую с мыши.")
public class RawMouseInput extends Module {

    private final ArrayList<Mouse> mouseList = new ArrayList<>();
    @Getter private boolean initialised, available;
    public static float deltaX, deltaY;
    private volatile boolean running;

    public static MouseThread thread;

    @Override
    public void onEnable() {
        if (!initialised) {
            initialised = true;
            available = true;

            try {
                ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();

                if (env.isSupported()) {
                    for (Controller controller : env.getControllers()) {
                        if (controller instanceof Mouse) mouseList.add((Mouse) controller);
                    }
                } else {
                    available = false;
                }
            } catch (Exception e) {
                available = false;
            }
        }

        running = true;
        thread = new MouseThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onDisable() {
        mc.gameSettings.invertMouse = false;
        running = false;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) mc.gameSettings.invertMouse = true;
    }

    public class MouseThread extends Thread {

        @Override
        public void run() {
            while (running) {
                available = !mouseList.isEmpty();

                for (Mouse mouse : mouseList) {
                    if (!mouse.poll()) continue;

                    float deltaX = mouse.getX().getPollData();
                    float deltaY = mouse.getY().getPollData();

                    if (org.lwjgl.input.Mouse.isGrabbed()) {
                        RawMouseInput.deltaX += deltaX;
                        RawMouseInput.deltaY += deltaY;
                    }
                }
            }
        }

        public void reset() {
            RawMouseInput.deltaX = 0;
            RawMouseInput.deltaY = 0;
        }
    }
}
