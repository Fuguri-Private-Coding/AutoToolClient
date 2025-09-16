package Effekseer.render;

import Effekseer.api.EffekseerManager;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.projection.MatrixUtils;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;

public class RenderEffects implements Imports {
    private static final float[] CAMERA_TRANSFORM_DATA = new float[16];
    private static final float[] PROJECTION_MATRIX_DATA = new float[16];

    EffekseerManager manager;

    public RenderEffects() {
        Client.INST.getEventManager().register(this);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (manager == null && Client.INST.getLoadNatives().getEffekseerManagerCore() != null) manager = new EffekseerManager(Client.INST.getLoadNatives().getEffekseerManagerCore());
        if (event instanceof Render3DEvent) {
            assert mc.thePlayer != null;
            assert mc.theWorld != null;

            ScaledResolution sc = new ScaledResolution(mc);
            glPushMatrix();

            MatrixUtils.getCameraAndProjectionMatrices(mc.timer.renderPartialTicks, CAMERA_TRANSFORM_DATA, PROJECTION_MATRIX_DATA);
            manager.setViewport(sc.getScaledWidth(), sc.getScaledHeight());
            manager.setCameraMatrix(CAMERA_TRANSFORM_DATA);
            manager.setProjectionMatrix(PROJECTION_MATRIX_DATA);
            manager.update(60 * getDeltaTime());
            glDisable(GL_ALPHA_TEST);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            manager.draw();

            glEnable(GL_ALPHA_TEST);
            glPopMatrix();
        }
    }

    private static float getDeltaTime() {
        long last = lastDrawTime;
        if (last == 0) {
            lastDrawTime = System.nanoTime();
            return 1f / 60;
        }

        long now = System.nanoTime();
        lastDrawTime = now;
        return (float) ((now - last) * 1e-9);
    }

    private static long lastDrawTime = 0;
}
